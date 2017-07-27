package example;

import org.evidently.annotations.Sink;

public class Test {

	@Sink("test") int p;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String s = "within(org.evidently.Package) && within( org.evidently.Package ) || within(org.evidently.Package )";
		
		String p = s.replaceAll("(within)\\((.*?)\\)", "$1(\"$2\")");
		
		System.out.println(p);

	}

}
