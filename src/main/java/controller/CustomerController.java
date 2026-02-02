package controller;

import dao.CustomerDAO;
import model.Customer;
import exception.DatabaseException;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class CustomerController {
    private CustomerDAO customerDAO;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{10,11}$");

    public CustomerController(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    // Lay tat ca khach hang
    public List<Customer> getAllCustomers() throws DatabaseException {
        try {
            return customerDAO.findAll();
        } catch (SQLException e) {
            throw new DatabaseException("Khong the tai danh sach khach hang", e);
        }
    }

    // Lay khach hang theo ID
    public Customer getCustomerById(int customerId) throws DatabaseException {
        try {
            Customer customer = customerDAO.findById(customerId);
            if (customer == null) {
                throw new DatabaseException("Khong tim thay khach hang");
            }
            return customer;
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi tim khach hang", e);
        }
    }

    // Tim khach hang theo email
    public Customer getCustomerByEmail(String email) throws DatabaseException {
        try {
            return customerDAO.findByEmail(email);
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi tim khach hang", e);
        }
    }

    // Tim kiem khach hang
    public List<Customer> searchCustomers(String keyword) throws DatabaseException {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllCustomers();
            }
            return customerDAO.searchByKeyword(keyword.trim());
        } catch (SQLException e) {
            throw new DatabaseException("Loi khi tim kiem khach hang", e);
        }
    }

    // Them khach hang moi
    public boolean addCustomer(Customer customer) throws DatabaseException {
        try {
            validateCustomer(customer);

            // Kiem tra email da ton tai
            if (customerDAO.emailExists(customer.getEmail())) {
                throw new DatabaseException("Email da ton tai");
            }

            return customerDAO.save(customer);
        } catch (SQLException e) {
            throw new DatabaseException("Khong the them khach hang", e);
        }
    }

    // Cap nhat khach hang
    public boolean updateCustomer(Customer customer) throws DatabaseException {
        try {
            validateCustomer(customer);
            return customerDAO.update(customer);
        } catch (SQLException e) {
            throw new DatabaseException("Khong the cap nhat khach hang", e);
        }
    }

    // Xoa khach hang
    public boolean deleteCustomer(int customerId) throws DatabaseException {
        try {
            return customerDAO.delete(customerId);
        } catch (SQLException e) {
            throw new DatabaseException("Khong the xoa khach hang", e);
        }
    }

    // Validate thong tin khach hang
    private void validateCustomer(Customer customer) throws DatabaseException {
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            throw new DatabaseException("Ten khong duoc de trong");
        }

        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            throw new DatabaseException("Email khong duoc de trong");
        }

        if (!EMAIL_PATTERN.matcher(customer.getEmail()).matches()) {
            throw new DatabaseException("Email khong hop le");
        }

        if (customer.getPhone() == null || customer.getPhone().trim().isEmpty()) {
            throw new DatabaseException("So dien thoai khong duoc de trong");
        }

        if (!PHONE_PATTERN.matcher(customer.getPhone()).matches()) {
            throw new DatabaseException("So dien thoai khong hop le (10-11 chu so)");
        }
    }
}