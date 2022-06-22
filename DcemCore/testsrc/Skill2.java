import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Skill2 {
	public static void main(String args[]) {
		List<String> list1 = new ArrayList<>();
		list1.add("Java");
		list1.add("ist");
		list1.add("eie");
		list1.add("Insel");
		list1.add("Java");
		
		Set <String> list2 = new HashSet<>();
		list2.add("Java");
		list2.add("Java");
		list2.add("Java");
		for(String value : list1) {
			
				list2.add(value);
		}
		System.out.println("Die neue Liste: " + list2);
	
/**
 *  Mit Lambda Expressions
 */
//	List<String> list3 = new ArrayList<>();
//
//	Collector collector = new Collector() {
//
//		@Override
//		public BiConsumer accumulator() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public Set characteristics() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public BinaryOperator combiner() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public Function finisher() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public Supplier supplier() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//		
//	};
//	Stream <String>  myStream = list1.stream().distinct().collect(collector)
	
//	Collector myCollector = new Collector() {
//
//		@Override
//		public BiConsumer accumulator() {
//			
//			return null;
//		}
//
//		@Override
//		public Set characteristics() {
//			
//			return null;
//		}
//
//		@Override
//		public BinaryOperator combiner() {
//			
//			return null;
//		}
//
//		@Override
//		public Function finisher() {
//			
//			return null;
//		}
//
//		@Override
//		public Supplier supplier() {
//			
//			return null;
//		}
//		
//	};
	
	}}
