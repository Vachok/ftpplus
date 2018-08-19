package ru.vachok.networker.workers;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.pasclass.Person;
import ru.vachok.networker.pasclass.PersonForm;

import java.util.ArrayList;
import java.util.List;


/**
 * @since 18.08.2018 (21:32)
 */
@Controller
public class NamesAndSur {

    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = NamesAndSur.class.getSimpleName();
    /**
     * {@link }
     */
    private static MessageToUser messageToUser = new MessageCons();
    private List<Person> persons = new ArrayList<>();
    @Value("${error.message}")
    private String errMessage;


    /**
     * Person list string.
     *
     * @param model the model
     * @return the string
     */
    @RequestMapping(value = {"/personList"}, method = RequestMethod.GET)
    public String personList( Model model ) {
        model.addAttribute("personList" , persons);
        return "personList";
    }


    /**
     * Show add person page string.
     *
     * @param model the model
     * @return the string
     */
    @RequestMapping(value = {"/addPerson"}, method = RequestMethod.GET)
    public String showAddPersonPage( Model model ) {
        String form = "personForm";
        model.addAttribute(form , form);
        return String.valueOf(persons);
    }


    /**
     * Save person string.
     *
     * @param model      the model
     * @param personForm the person form
     * @return the string
     */
    @RequestMapping(value = {"/addPerson"}, method = RequestMethod.POST)
    public String savePerson( Model model , @ModelAttribute("personForm") PersonForm personForm ) {
        persons.add(new Person("Ola" , "Barchi"));
        persons.add(new Person("Ivan" , "Do"));
        String firstName = personForm.getFirstName();
        String lastName = personForm.getLastName();
        if (firstName != null && firstName.length() > 0 && lastName != null && lastName.length() > 0) {
            Person newPerson = new Person(firstName , lastName);
            persons.add(newPerson);
            newPerson.writeToWriter();
            return "redirect:/";
        }
        model.addAttribute("errorMessage" , errMessage);
        return "personForm";
    }
}