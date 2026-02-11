package util;

import model.Customer;

public class Session {

    private static Customer currentCustomer;

    public static Customer getCurrentCustomer() {
        return currentCustomer;
    }

    public static void setCurrentCustomer(Customer customer) {
        currentCustomer = customer;
    }

    public static void clear() {
        currentCustomer = null;
    }
}
