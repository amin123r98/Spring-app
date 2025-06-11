import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        while (true) {


            String[][] nums = new String[][]
                    {
                            {"_", "_", "_"},
                            {"_", "_", "_"},
                            {"_", "_", "_"}
                    };
            Scanner scanner = new Scanner(System.in);
            System.out.print("ввидите 0/x:");
            String n = scanner.nextLine();
            System.out.print("ввидите число:");
            int num = scanner.nextByte();


            switch (num) {

                case 1:

                    nums[0][0] = n;
                    break;
                case 2:
                    nums[0][1] = n;
                    break;
                case 3:
                    nums[0][2] = n;
                    break;
                case 4:
                    nums[1][0] = n;
                    break;
                case 5:
                    nums[1][1] = n;
                    break;
                case 6:
                    nums[1][2] = n;
                    break;
                case 7:
                    nums[2][0] = n;
                    break;
                case 8:
                    nums[2][1] = n;
                    break;
                case 9:
                    nums[2][2] = n;
                    break;
            }


            for (int i = 0; i < nums.length; i++) {
                for (int j = 0; j < nums[i].length; j++) {


                    System.out.printf(nums[i][j] + "|");

                }
                System.out.println();
            }


        }
    }}
