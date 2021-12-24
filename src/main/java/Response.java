
public class Response {

    private String url;
    private Integer time;

    public Response(String url, Integer time) {
        this.time = time;
        this.url = url;
    }

    public Integer getTime() {
        return time;
    }

    public String getUrl() {
        return url;
    }
}
