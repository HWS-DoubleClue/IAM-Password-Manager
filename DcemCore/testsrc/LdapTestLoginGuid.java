
import java.util.Base64;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;

import com.doubleclue.utils.StringUtils;

/**
 * Example code for retrieving a Users Primary Group from Microsoft Active
 * Directory via. its LDAP API
 * 
 * @author Adam Retter <adam.retter@googlemail.com>
 */
public class LdapTestLoginGuid {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) throws NamingException {

		final String ldapAdServer = "ldap://hws002s0004.dom1.ad.tld";
		final String ldapSearchBase = "OU=HWS_Group,DC=dom1,DC=ad,DC=tld";

		final String ldapUsername = "dom1\\svc_AD_lesen";
		final String ldapPassword = "BUnwWLZ6kL";

		final String ldapAccountToLookup = "emanuel.galea";

		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		if (ldapUsername != null) {
			env.put(Context.SECURITY_PRINCIPAL, ldapUsername);
		}
		if (ldapPassword != null) {
			env.put(Context.SECURITY_CREDENTIALS, ldapPassword);
		}
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapAdServer);

		// ensures that objectSID attribute values
		// will be returned as a byte[] instead of a String
		env.put("java.naming.ldap.attributes.binary", "objectGUID");

		// the following is helpful in debugging errors
		// env.put("com.sun.jndi.ldap.trace.ber", System.err);

		LdapContext ctx = new InitialLdapContext(env, null);

		byte[] guid = null;
		String guidHex = null;
		// 1) lookup the ldap account
		Map<String, Attributes> ldapTree = findAccountByAccountName(ctx, ldapSearchBase, ldapAccountToLookup);
		for (String dn : ldapTree.keySet()) {
			System.out.println("DN: " + dn + ",  " + ldapTree.get(dn).get("sAMAccountName"));
			guid = (byte[]) ldapTree.get(dn).get("objectGUID").get();
			guidHex = StringUtils.getHexStringRaw(guid);
			System.out.println("ObjectGUID: " + guidHex+ ", Base64: " + Base64.getEncoder().encodeToString(guid));
			LdapName ldapName = new LdapName(dn);
			ldapName.getRdns();
		}
		System.out.println("Records Found: " + ldapTree.size());

		env.put(Context.SECURITY_PRINCIPAL, "test@hws-gruppe.de");
		env.put(Context.SECURITY_CREDENTIALS, "xxxxx");
		try {
			ctx = new InitialLdapContext(env, null);
			System.out.println("LdapTestLoginGuid.main() PROSIT"); 

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.exit(0);

		// Map<String, Attributes> ldapTreeGroup = findGroups(ctx, ldapSearchBase, ldapAccountToLookup);
		// for (String dn : ldapTreeGroup.keySet()) {
		// System.out.println("DN: " + dn + ", " + ldapTreeGroup.get(dn).get("name"));
		//
		// }
		// System.out.println("Groups Found: " + ldapTreeGroup.size());

	}

	public static void printSearchEnumeration(NamingEnumeration<?> retEnum) {
		try {
			while (retEnum.hasMore()) {
				SearchResult sr = (SearchResult) retEnum.next();
				System.out.println(">>" + sr.getNameInNamespace());
			}
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	static public Map<String, Attributes> findAccountByAccountName(DirContext ctx, String ldapSearchBase, String accountName) throws NamingException {

		String searchFilter = "(&(objectClass=person)(sAMAccountName=" + accountName + ")(UserAccountControl:1.2.840.113556.1.4.803:=512)"
				+ "(!(UserAccountControl:1.2.840.113556.1.4.803:=2)) )";

		Map<String, Attributes> map = new TreeMap<>();
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String returnedAtts[] = { "distinguishedName", "mail", "givenName", "sn", "telephone", "mobile", "sAMAccountName", "objectGUID" };
		searchControls.setReturningAttributes(returnedAtts);
		NamingEnumeration<SearchResult> results = null;
		SearchResult searchResult = null;
		try {
			results = ctx.search(ldapSearchBase, searchFilter, searchControls);
			while (results.hasMore()) {
				searchResult = results.nextElement();
				map.put(searchResult.getName(), searchResult.getAttributes());
			}

		} catch (Exception exp) {
			exp.printStackTrace();
			System.exit(0);
		}

		return map;
	}

	static public Map<String, Attributes> findGroups(DirContext ctx, String ldapSearchBase, String accountName) throws NamingException {

		String searchFilter = "(&(objectClass=group))";

		Map<String, Attributes> map = new TreeMap<>();
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		String returnedAtts[] = { "distinguishedName", "cn", "name" };
		searchControls.setReturningAttributes(returnedAtts);
		NamingEnumeration<SearchResult> results = null;
		SearchResult searchResult = null;
		try {
			results = ctx.search(ldapSearchBase, searchFilter, searchControls);
			while (results.hasMore()) {
				searchResult = results.nextElement();
				map.put(searchResult.getName(), searchResult.getAttributes());
			}

		} catch (Exception exp) {
			exp.printStackTrace();
			System.exit(0);
		}

		return map;
	}

}
