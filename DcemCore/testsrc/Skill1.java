
public class Skill1 {
	public static void main(String args[]) {
		Integer intObj = Integer.valueOf(args[args.length - 1]);
		int i = intObj.intValue();

		if (args.length > 1)
			System.out.print(args[i-1]);
		if (args.length > 0)
			System.out.println(i - 1);
		else
			System.out.println(i - 2);
		
		int in,j,k,l=0;
	k = l++;
	j = ++k;
	in = j++;
	System.out.println(in);		
	}

//	public static void main(String[] args) {
//		helpdesk
//	}
//
//	public static void main(String[] args) {
//		helpdesk
//	}
}
