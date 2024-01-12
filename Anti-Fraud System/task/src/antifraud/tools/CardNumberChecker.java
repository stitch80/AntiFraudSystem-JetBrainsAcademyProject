package antifraud.tools;

public class CardNumberChecker {

    public static boolean isValid(String number) {
        String lastDigit = number.substring(number.length() - 1);
        String panWithoutLastDigit = number.substring(0, number.length() - 1);
        int controlNumber = calculateControlNumber(panWithoutLastDigit);
        return Integer.parseInt(lastDigit) == controlNumber;
    }
    private static int calculateControlNumber(String input) {
        int output = 0;
        String[] inputStrings = input.split("");
        for (int i = 0; i < inputStrings.length; i++) {
            if (i % 2 == 0) {
                int temp = Integer.parseInt(inputStrings[i]) * 2;
                output += temp > 9 ? temp - 9 : temp;
            } else {
                output += Integer.parseInt(inputStrings[i]);
            }
        }
        return output % 10 == 0 ? 0 : 10 - (output % 10);
    }
}
