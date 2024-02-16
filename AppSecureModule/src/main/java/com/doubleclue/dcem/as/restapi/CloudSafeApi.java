package com.doubleclue.dcem.as.restapi;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.doubleclue.dcem.as.restapi.impl.CloudSafeApiServiceImpl;
import com.doubleclue.dcem.core.jersey.DcemApiException;
import com.doubleclue.dcem.core.jpa.ApiFilterItem;
import com.doubleclue.dcem.core.weld.CdiUtils;

@Path("as/cloudSafe")
@Consumes({ "application/json" })
@Produces({ "application/json" })

public class CloudSafeApi {

//	@POST
//	@Path("/deleteCloudSafeFile")
//	@Consumes({ "application/json" })
//	@Produces({ "application/json" })
//	public Response deleteCloudSafeFile(AsApiCloudSafeFile cloudSafeFile, @Context SecurityContext securityContext) throws ApiException {
//		CloudSafeApiServiceImpl apiServiceImpl = CdiUtils.getReference(CloudSafeApiServiceImpl.class);
//		return apiServiceImpl.deleteCloudSafeFile(cloudSafeFile, securityContext);
//	}

//	@POST
//	@Path("/readCloudSafeFile")
//	@Consumes({ "application/json" })
//	@Produces({ "application/json" })
//	public Response readCloudSafeFile(AsApiCloudSafeFile cloudSafeFile, @Context SecurityContext securityContext) throws ApiException {
//		CloudSafeApiServiceImpl apiServiceImpl = CdiUtils.getReference(CloudSafeApiServiceImpl.class);
//		return apiServiceImpl.readCloudSafeFile(cloudSafeFile, securityContext);
//	}

	@POST
	@Path("/queryCloudSafe")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response queryCloudSafe(List<ApiFilterItem> filterItems, @QueryParam("offset") int offset, @QueryParam("maxResults") int maxResults,
			@Context SecurityContext securityContext) throws DcemApiException {
		CloudSafeApiServiceImpl apiServiceImpl = CdiUtils.getReference(CloudSafeApiServiceImpl.class);
		return apiServiceImpl.queryCloudSafe(filterItems, offset, maxResults, securityContext);
	}

//	@POST
//	@Path("/writeCloudSafeFile")
//	@Consumes({ "application/json" })
//	@Produces({ "application/json" })
//	public Response writeCloudSafeFile(AsApiCloudSafeFile cloudSafeFile, @QueryParam("userLoginId") String userLoginId,
//			@Context SecurityContext securityContext) throws NotFoundException {
//		CloudSafeApiServiceImpl apiServiceImpl = CdiUtils.getReference(CloudSafeApiServiceImpl.class);
//		return apiServiceImpl.writeCloudSafeFile(cloudSafeFile, userLoginId, securityContext);
//	}

//	@POST
//	@Path("/shareCloudSafeFile")
//	@Consumes({ "application/json" })
//	@Produces({ "application/json" })
//	public Response shareCloudSafeFile(AsApiShareCloudSafe shareCloudSafe, @Context SecurityContext securityContext) throws ApiException {
//		CloudSafeApiServiceImpl apiServiceImpl = CdiUtils.getReference(CloudSafeApiServiceImpl.class);
//		return apiServiceImpl.shareCloudSafeFile(shareCloudSafe, securityContext);
//	}

//	@POST
//	@Path("/removeShareCloudSafeFile")
//	@Consumes({ "application/json" })
//	@Produces({ "application/json" })
//	public Response removeShareCloudSafeFile(AsApiShareCloudSafe shareCloudSafe, @Context SecurityContext securityContext) throws ApiException {
//		CloudSafeApiServiceImpl apiServiceImpl = CdiUtils.getReference(CloudSafeApiServiceImpl.class);
//		return apiServiceImpl.removeShareCloudSafeFile(shareCloudSafe, securityContext);
//	}

//	@GET
//	@Path("/getSharedCloudSafeFiles")
//	@Consumes({ "application/json" })
//	@Produces({ "application/json" })
//	public Response getShareCloudSafeFiles(@QueryParam("userLoginId") String userLoginId, @QueryParam("nameFilter") String nameFilter,
//			@Context SecurityContext securityContext) throws NotFoundException {
//		CloudSafeApiServiceImpl apiServiceImpl = CdiUtils.getReference(CloudSafeApiServiceImpl.class);
//		return apiServiceImpl.getShareCloudSafeFiles(userLoginId, nameFilter, securityContext);
//	}

//	@POST
//	@Path("/getSharedCloudSafeUsersAccess")
//	@Consumes({ "application/json" })
//	@Produces({ "application/json" })
//	public Response getSharedCloudSafeUsersAccess(AsApiCloudSafeFile cloudSafeFile, @Context SecurityContext securityContext) throws ApiException {
//		CloudSafeApiServiceImpl apiServiceImpl = CdiUtils.getReference(CloudSafeApiServiceImpl.class);
//		return apiServiceImpl.getSharedCloudSafeUsersAccess(cloudSafeFile, securityContext);
//	}
}
