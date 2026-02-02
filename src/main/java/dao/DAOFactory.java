package dao;

public class DAOFactory {
    private static DAOFactory instance;

    private MovieDAO movieDAO;
    private ShowtimeDAO showtimeDAO;
    private SeatDAO seatDAO;
    private BookingDAO bookingDAO;
    private CustomerDAO customerDAO;
    private PaymentDAO paymentDAO;
    private BookingSeatDAO bookingSeatDAO;
    private StatisticsDAO statisticsDAO;
    private ViewDAO viewDAO;

    private DAOFactory() {
        movieDAO = new MovieDAO();
        showtimeDAO = new ShowtimeDAO();
        seatDAO = new SeatDAO();
        bookingDAO = new BookingDAO();
        customerDAO = new CustomerDAO();
        paymentDAO = new PaymentDAO();
        bookingSeatDAO = new BookingSeatDAO();
        statisticsDAO = new StatisticsDAO();
        viewDAO = new ViewDAO();
    }

    public static synchronized DAOFactory getInstance() {
        if (instance == null) {
            instance = new DAOFactory();
        }
        return instance;
    }

    public MovieDAO getMovieDAO() {
        return movieDAO;
    }

    public ShowtimeDAO getShowtimeDAO() {
        return showtimeDAO;
    }

    public SeatDAO getSeatDAO() {
        return seatDAO;
    }

    public BookingDAO getBookingDAO() {
        return bookingDAO;
    }

    public CustomerDAO getCustomerDAO() {
        return customerDAO;
    }

    public PaymentDAO getPaymentDAO() {
        return paymentDAO;
    }

    public BookingSeatDAO getBookingSeatDAO() {
        return bookingSeatDAO;
    }

    public StatisticsDAO getStatisticsDAO() {
        return statisticsDAO;
    }

    public ViewDAO getViewDAO() {
        return viewDAO;
    }
}