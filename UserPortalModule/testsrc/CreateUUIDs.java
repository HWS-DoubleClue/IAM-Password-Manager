import java.util.UUID;

public class CreateUUIDs {

	public static void main(String[] args) {
		UUID entryIcon = UUID.randomUUID();
		System.out.println("CreateUUIDs.main() Entry: " + entryIcon.toString());
				
		UUID groupIcon = UUID.randomUUID();
		System.out.println("CreateUUIDs.main() group: " + groupIcon.toString());
		
		UUID binIcon = UUID.randomUUID();
		System.out.println("CreateUUIDs.main() Bin: " + binIcon.toString());
		

	}

}
