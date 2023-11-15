import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public class LookupIp {

	public static void main(String[] args) {
		System.out.println("LookupIp.main() " + getHostName("212.185.199.7"));
		try {

			Hashtable env = new Hashtable();
			env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");

			DirContext ctx = new InitialDirContext(env);
			Attributes attrs = ctx.getAttributes("7.199.185.212.in-addr.arpa", new String[] { "PTR" });

			for (NamingEnumeration ae = attrs.getAll(); ae.hasMoreElements();) {
				Attribute attr = (Attribute) ae.next();
				String attrId = attr.getID();
				for (Enumeration vals = attr.getAll(); vals.hasMoreElements(); System.out.println(attrId + ": " + vals.nextElement()))
					;
			}

			ctx.close();
		}

		catch (Exception e) {
			System.out.println("NO REVERSE DNS");
		}
	}
	
	private static String getHostName(final String ip)
	 {
	   String retVal = null;
	   final String[] bytes = ip.split("\\.");
	   if (bytes.length == 4)
	   {
	      try
	      {
	         final java.util.Hashtable<String, String> env = new java.util.Hashtable<String, String>();
	         env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
	         final javax.naming.directory.DirContext ctx = new javax.naming.directory.InitialDirContext(env);
	         final String reverseDnsDomain = bytes[3] + "." + bytes[2] + "." + bytes[1] + "." + bytes[0] + ".in-addr.arpa";
	         final javax.naming.directory.Attributes attrs = ctx.getAttributes(reverseDnsDomain, new String[]
	         {
	            "PTR",
	         });
	         for (final javax.naming.NamingEnumeration<? extends javax.naming.directory.Attribute> ae = attrs.getAll(); ae.hasMoreElements();)
	         {
	            final javax.naming.directory.Attribute attr = ae.next();
	            final String attrId = attr.getID();
	            for (final java.util.Enumeration<?> vals = attr.getAll(); vals.hasMoreElements();)
	            {
	               String value = vals.nextElement().toString();
	               // System.out.println(attrId + ": " + value);

	               if ("PTR".equals(attrId))
	               {
	                  final int len = value.length();
	                  if (value.charAt(len - 1) == '.')
	                  {
	                     // Strip out trailing period
	                     value = value.substring(0, len - 1);
	                  }
	                  retVal = value;
	               }
	            }
	         }
	         ctx.close();
	      }
	      catch (final javax.naming.NamingException e)
	      {
	         // No reverse DNS that we could find, try with InetAddress
	         System.out.print(""); // NO-OP
	      }
	   }

	   if (null == retVal)
	   {
	      try
	      {
	         retVal = java.net.InetAddress.getByName(ip).getCanonicalHostName();
	      }
	      catch (final java.net.UnknownHostException e1)
	      {
	         retVal = ip;
	      }
	   }

	   return retVal;
	 }

}
