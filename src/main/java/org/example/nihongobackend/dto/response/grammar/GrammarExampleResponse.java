package org.example.nihongobackend.dto.response.grammar;

public class GrammarExampleResponse {

    private String ja;
    private String vi;
    /** polite | casual | null */
    private String register;

    public String getJa() {
        return ja;
    }

    public void setJa(String ja) {
        this.ja = ja;
    }

    public String getVi() {
        return vi;
    }

    public void setVi(String vi) {
        this.vi = vi;
    }

    public String getRegister() {
        return register;
    }

    public void setRegister(String register) {
        this.register = register;
    }
}
