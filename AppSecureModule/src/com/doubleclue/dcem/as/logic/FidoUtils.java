package com.doubleclue.dcem.as.logic;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yubico.internal.util.JacksonCodecs;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AssertionExtensionInputs;
import com.yubico.webauthn.data.AttestationConveyancePreference;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.RegistrationExtensionInputs;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.exception.Base64UrlException;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;

public class FidoUtils {

	// These classes are adapted from com.yubico.internal.util.json to their Gson equivalents

	/*private static class JsonStringSerializer implements JsonSerializer<JsonStringSerializable> {
		public JsonElement serialize(JsonStringSerializable src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.toJsonString());
		}
	}

	private static class JsonLongSerializer implements JsonSerializer<JsonLongSerializable> {
		public JsonElement serialize(JsonLongSerializable src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.toJsonNumber());
		}
	}

	private static class LocalDateJsonSerializer implements JsonSerializer<LocalDate> {
		public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.toString());
		}
	}

	private static final Gson yubicoGson = new GsonBuilder().registerTypeHierarchyAdapter(JsonStringSerializable.class, new JsonStringSerializer())
			.registerTypeHierarchyAdapter(JsonLongSerializable.class, new JsonLongSerializer())
			.registerTypeHierarchyAdapter(LocalDate.class, new LocalDateJsonSerializer()).create();*/

	/*private static ObjectMapper yubicoMapper = null;
	
	private static ObjectMapper getYubicoMapper() {
		if (yubicoMapper == null) {
			SimpleModule module = new SimpleModule();
			module.addSerializer(JsonStringSerializable.class, new JsonStringSerializer<>());
			module.addSerializer(JsonLongSerializable.class, new JsonLongSerializer<>());
			module.addSerializer(LocalDate.class, new LocalDateJsonSerializer());
			yubicoMapper = new ObjectMapper()
					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
					.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
					.setSerializationInclusion(Include.NON_ABSENT)
					.setBase64Variant(Base64Variants.MODIFIED_FOR_URL)
					.registerModule(module);
		}
		return yubicoMapper;
	}*/

	public static String getJson(Object o) throws JsonProcessingException {
		// return getYubicoMapper().writeValueAsString(o);
		//return yubicoGson.toJson(o);
		return JacksonCodecs.json().writeValueAsString(o);
	}

	public static RelyingParty createRelyingParty(String id, String name, CredentialRepository repository, Set<String> origins) {
		RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder().id(id).name(name).build();
		return RelyingParty.builder().identity(rpIdentity).credentialRepository(repository).origins(origins)
				.attestationConveyancePreference(AttestationConveyancePreference.DIRECT).build();
	}

	public static PublicKeyCredentialCreationOptions createRegisterRequest(RelyingParty rp, String name, String displayName, ByteArray handle,
			boolean passwordless) {
		UserIdentity userId = UserIdentity.builder().name(name).displayName(displayName).id(handle).build();
		RegistrationExtensionInputs extensions = RegistrationExtensionInputs.builder().build();
		AuthenticatorSelectionCriteria asc = AuthenticatorSelectionCriteria.builder().requireResidentKey(passwordless).build();
		StartRegistrationOptions sro = StartRegistrationOptions.builder().user(userId).extensions(extensions).authenticatorSelection(asc).build();
		return rp.startRegistration(sro);
	}

	public static RegistrationResult validateRegisterResponse(RelyingParty rp, PublicKeyCredentialCreationOptions request,
			PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> response) throws RegistrationFailedException {
		return rp.finishRegistration(FinishRegistrationOptions.builder().request(request).response(response).build());
	}

	public static AssertionRequest createAssertRequest(RelyingParty rp, String username) {
		AssertionExtensionInputs extensions = AssertionExtensionInputs.builder().build();
		return rp.startAssertion(StartAssertionOptions.builder().username(username).extensions(extensions).build());
	}

	public static AssertionResult validateAssertResponse(RelyingParty rp, AssertionRequest request,
			PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> response)
			throws IOException, Base64UrlException, AssertionFailedException {

		// U2F backwards-compatibility
		Optional<ByteArray> userHandle = response.getResponse().getUserHandle();
		if (!userHandle.isPresent() || userHandle.get().isEmpty()) {
			if (request.getUsername().isPresent()) {
				String username = request.getUsername().get();
				Optional<ByteArray> userHandleFromDb = rp.getCredentialRepository().getUserHandleForUsername(username);
				response = response.toBuilder().response(response.getResponse().toBuilder().userHandle(userHandleFromDb).build()).build();
			}
		}

		// validate response
		return rp.finishAssertion(FinishAssertionOptions.builder().request(request).response(response).build());
	}
}
