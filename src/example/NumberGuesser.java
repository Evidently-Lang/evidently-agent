package example;


import org.evidently.annotations.Sink;
import org.evidently.annotations.Source;

public class NumberGuesser {

	@Sink("DB")
	@Source("DB")
	private int realNumber = 2600;
	@Sink("DB")
	@Source("DB")
	public boolean admin = false;
//
	public boolean checkPassword(@Sink({ "DB", "LOG" }) @Source({ "UI" }) int guess) {

		boolean isOK = false;

		// : conditional icf error
		if (guess == realNumber) {
			isOK = true;
		}

		writeLog(guess); // OK

		// HERE we need to check this.

		// if(admin){
		writeLog(realNumber); // not OK
		// }

		return isOK;
	}
	public void writeLog(@Sink({ "LOG" }) int msg) {
		System.out.println("" + msg);
	}

	public void writeLog(@Sink("LOG") String msg) {
		System.out.println(msg);
	}

	public static void main(String args[]) {

		@Sink({ "DB", "LOG" })
		@Source("UI")
		int guess = 2600;

		@Sink({ "LITERAL" })
		@Source("LITERAL")
		NumberGuesser checker = new NumberGuesser();

		boolean isOK = checker.checkPassword(guess);

		if (isOK) {
			checker.writeLog("Login OK");
		} else {
			checker.writeLog("Invalid Username or Password");
		}
	}
}
