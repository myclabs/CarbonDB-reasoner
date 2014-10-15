package com.mycsense.carbondb.domain;

public class Reference {
    protected String title;
    protected String source;
    protected String URL;
    protected String creator;
    protected String publisher;
    protected String date;
    protected String URI;

    public Reference(String title, String source, String URL, String creator, String publisher, String date, String URI) {
        this.title = title;
        this.source = source;
        this.URL = URL;
        this.creator = creator;
        this.publisher = publisher;
        this.date = date;
        this.URI = URI;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }
}
