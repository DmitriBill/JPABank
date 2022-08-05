package com.gmail.practise;

public class CurrencyConverter {

    private static final Double HRYVNIA = 1.0;
    private static final Double DOLLAR = 1.0 / 28.0;
    private static final Double EURO = 1.0 / 32.0;


    public static boolean convert(Client client, String fromCurrency, String toCurrency) {
        if (!converter(client, fromCurrency, toCurrency))
            return false;
        Account accountFrom = client.getAccountByCurrency(fromCurrency);
        Account accountTo = client.getAccountByCurrency(toCurrency);
        Double balanceFrom = accountFrom.getBalance();
        Double balanceTo = accountTo.getBalance();
        Double kf = 1.0;
        switch (fromCurrency) {
            case "UAH":
                if (toCurrency.equals("EUR")) {
                    kf = EURO;
                } else {
                    kf = DOLLAR;
                }
                break;
            case "USD":
                if (toCurrency.equals("EUR")) {
                    kf = EURO / DOLLAR;
                } else {
                    kf = HRYVNIA / DOLLAR;
                }
                break;
            case "EUR":
                if (toCurrency.equals("USD")) {
                    kf = DOLLAR / EURO;
                } else {
                    kf = HRYVNIA / EURO;
                }
                break;
        }
        client.getAccountByCurrency(toCurrency).setBalance(balanceTo + balanceFrom * kf);
        client.getAccountByCurrency(fromCurrency).setBalance(0.0);
        return true;
    }

    private static boolean converter(Client client, String fromCurrency, String toCurrency) {
        if (!("UAH".equals(fromCurrency) || "USD".equals(fromCurrency) || "EUR".equals(fromCurrency))
                && ("UAH".equals(toCurrency) || "USD".equals(toCurrency) || "EUR".equals(toCurrency))) {
            System.out.println("Enter the correct currency");
            return false;
        }

        Account accountFrom = client.getAccountByCurrency(fromCurrency);
        if (accountFrom==null || accountFrom.getBalance()<=0){
            System.out.println("Not enough money");
            return false;
        }

        return true;
    }
}
