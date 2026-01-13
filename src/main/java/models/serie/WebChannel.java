package models.serie;

public class WebChannel {
    private int id;
    private String name;
    private Country country;
    private String officialSite;

    public int getId() { return id; }
    public String getName() { return name; }
    public Country getCountry() { return country; }
    public String getOfficialSite() { return officialSite; }

    public static class Country {
        private String name;
        private String code;
        private String timezone;

        public String getName() { return name; }
        public String getCode() { return code; }
        public String getTimezone() { return timezone; }
    }
}