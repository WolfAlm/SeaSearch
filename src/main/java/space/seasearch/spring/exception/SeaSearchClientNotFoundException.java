package space.seasearch.spring.exception;

public class SeaSearchClientNotFoundException extends Exception {

    private String userPhone;

    public SeaSearchClientNotFoundException(String userPhone) {
        this.userPhone = userPhone;
    }
}
