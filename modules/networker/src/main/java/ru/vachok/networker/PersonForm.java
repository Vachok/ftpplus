package ru.vachok.networker;


/**
 * <h1>Тест-форма</h1>
 <p>
 (WEB-INF/addPerson.html)
 *
 * @since 11.08.2018 (21:29)
 */
public class PersonForm {

    /**
     * <b>Имя</b>
     */
    private String firstName;
    private String lastName;


    /**
     * <b>Геттер имени</b>
     *
     * @return имя
     */
    public String getFirstName() {
        return firstName;
    }


    /**
     *<b>Сеттер имени</b>
     *
     * @param firstName фамилия
     */
    public void setFirstName( String firstName ) {
        this.firstName = firstName;
    }


    /**
     * <b>Геттер фамилии</b>
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }


    /**
     * <b>Сеттер фамилии</b>
     *
     * @param lastName the last name
     */
    public void setLastName( String lastName ) {
        this.lastName = lastName;
    }
}