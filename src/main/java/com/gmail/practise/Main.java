package com.gmail.practise;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.Scanner;

public class Main {
    static EntityManagerFactory emf;
    static EntityManager em;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try {
            // create connection
            emf = Persistence.createEntityManagerFactory("JPABank");
            em = emf.createEntityManager();
            CurrencyRates.createRates(em);
            try {
                while (true) {
                    System.out.println("1: Add new client");
                    System.out.println("2: Add new account");
                    System.out.println("3: Complete account");
                    System.out.println("4: Converter");
                    System.out.println("5: Open rates");
                    System.out.println("6: Open transactions");
                    System.out.println("7: Open clients");
                    System.out.println("8: Open accounts");

                    System.out.print("-> ");

                    String s = sc.nextLine();
                    switch (s) {
                        case "1":
                            addNewClient();
                            break;
                        case "2":
                            addNewAccount();
                            break;
                        case "3":
                            completeAccount();
                            break;
                        case "4":
                            converter();
                            break;
                        case "5":
                            openRates();
                            break;
                        case "6":
                            openTransactionsCriteria();
                            break;
                        case "7":
                            openClientsCriteria();
                            break;
                        case "8":
                            openAccountsCriteria();
                            break;
                        case "9":
                            sendMoney();
                            break;

                        default:
                            continue;
                    }
                }
            } finally {
                sc.close();
                em.close();
                emf.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    private static void addNewClient() {
        Client client = getClientById();
        if (client == null)
            return;
        System.out.print("Enter account currency: ");
        String currency = sc.nextLine();

        Account account = new Account(currency);
        if (client != null) {
            client.addAccount(account);
        } else {
            return;
        }
        em.getTransaction().begin();
        try {
            em.persist(client);
            em.getTransaction().commit();
        } catch (Exception ex) {
            em.getTransaction().rollback();
        }
    }

    private static void addNewAccount() {
        System.out.print("Enter client's name: ");
        String name = sc.nextLine();
        Client client = new Client(name);

        em.getTransaction().begin();
        try {
            em.persist(client);
            em.getTransaction().commit();
        } catch (Exception ex) {
            em.getTransaction().rollback();
        }
    }

    private static void completeAccount() {
        Client client = getClientById();
        if (client == null)
            return;

        System.out.print("Enter currency to complete: ");
        String currencyToRefill = sc.nextLine();
        System.out.print("Enter number: ");
        String sAmount = sc.nextLine();
        Double amount = Double.parseDouble(sAmount);
        Account account = client.getAccountByCurrency(currencyToRefill);
        account.refillBalance(amount);
        Transaction transaction = new Transaction(account, account, amount);

        em.getTransaction().begin();
        try {
            em.persist(client);
            em.getTransaction().commit();
            addTransaction(transaction);

        } catch (Exception e) {
            em.getTransaction().rollback();
        }
    }

    private static void converter() {
        Client client = getClientById();
        if (client == null)
            return;

        System.out.print("Enter currency convert: ");
        String currencyFrom = sc.nextLine();
        System.out.print("Enter currency convert to: ");
        String currencyTo = sc.nextLine();
        if (CurrencyConverter.convert(client, currencyFrom, currencyTo)) {
            Transaction transaction = new Transaction(
                    client.getAccountByCurrency(currencyFrom), client.getAccountByCurrency(currencyTo), client.getAccountByCurrency(currencyFrom).getBalance());
            em.getTransaction().begin();
            try {
                em.persist(client);
                em.getTransaction().commit();
                addTransaction(transaction);
            } catch (Exception ex) {
                em.getTransaction().rollback();
            }
        } else {
            System.out.println("Converting failed");
        }
    }

    private static void openRates() {
        CurrencyRates.updateRates(em);
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<CurrencyRates> criteria = builder.createQuery(CurrencyRates.class);
        Root<CurrencyRates> root = criteria.from(CurrencyRates.class);
        criteria.select(root);
        List<CurrencyRates> res1 = em.createQuery(criteria).getResultList();
        for (CurrencyRates c : res1)
            System.out.println(c.getName() + "=" + c.getRate());
    }

    public static void openTransactionsCriteria() {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Transaction> criteria = builder.createQuery(Transaction.class);
        Root<Transaction> root = criteria.from(Transaction.class);
        criteria.select(root);
        List<Transaction> res1 = em.createQuery(criteria).getResultList();
        for (Transaction transaction : res1)
            System.out.println(transaction);
    }

    public static void openClientsCriteria() {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Client> criteria = builder.createQuery(Client.class);
        Root<Client> root = criteria.from(Client.class);
        criteria.select(root);
        List<Client> res1 = em.createQuery(criteria).getResultList();
        for (Client client : res1)
            System.out.println(client);
    }

    public static void openAccountsCriteria() {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Account> criteria = builder.createQuery(Account.class);
        Root<Account> root = criteria.from(Account.class);
        criteria.select(root);
        List<Account> res1 = em.createQuery(criteria).getResultList();
        for (Account account : res1)
            System.out.println(account);
    }

    private static void sendMoney() {
        System.out.print("Enter sender money id: ");
        Client clientFrom = getClientById();
        if (clientFrom == null)
            return;
        System.out.print("Enter receiver money id: ");
        Client clientTo = getClientById();
        if (clientTo == null)
            return;

        System.out.print("Enter currency: ");
        String currency = sc.nextLine();
        Account accountFrom = clientFrom.getAccountByCurrency(currency);
        Account accountTo = clientTo.getAccountByCurrency(currency);
        accountTo.setBalance(accountTo.getBalance() + accountFrom.getBalance());
        accountFrom.setBalance(0.0);
        em.getTransaction().begin();
        try {
            em.merge(accountFrom);
            em.merge(accountTo);
        } catch (Exception e) {
            em.getTransaction().rollback();
        }
    }

    private static void addTransaction(Transaction transaction) {
        em.getTransaction().begin();
        try {
            em.persist(transaction);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
        }
    }

    private static Client getClientById() {
        System.out.print("Enter client id: ");
        String sId = sc.nextLine();
        Integer id = Integer.parseInt(sId);
        Client client = em.find(Client.class, id);
        if (client == null) {
            System.out.println("Client with id=" + id + " not found");
            return null;
        }
        return client;
    }
}
