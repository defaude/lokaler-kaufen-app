package de.qaware.mercury.cli.impl;

import de.qaware.mercury.cli.Console;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
class ConsoleImpl implements Console {
    private final Scanner scanner;

    public ConsoleImpl() {
        scanner = new Scanner(System.in);
    }

    @Override
    public void print(String text) {
        System.out.print(text);
    }

    @Override
    public void printLine(String text) {
        System.out.println(text);
    }

    @Override
    public String readLine() {
        return scanner.nextLine();
    }

    @Override
    public String readPassword() {
        return scanner.nextLine();
    }

    @Override
    public boolean readBoolean(boolean theDefault) {
        while (true) {
            String line = scanner.nextLine();
            if (line.isEmpty()) { // User didn't input something
                return theDefault;
            }

            switch (line.toLowerCase()) {
                case "y":
                    return true;
                case "n":
                    return false;
                default:
                    print("Invalid input, please try again: ");
            }
        }
    }
}
