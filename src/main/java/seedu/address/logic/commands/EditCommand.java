package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_TAG_NOT_IN_TAG_LIST;
//import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
//import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
//import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

//import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
//import seedu.address.model.person.Address;
//import seedu.address.model.person.Email;
import seedu.address.model.person.Id;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
//import seedu.address.model.tag.Tag;
import seedu.address.model.tag.Tag;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = ">";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the person identified "
            + "by the id in the displayed person list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: ID"
            + PREFIX_NAME + "NAME "
            + PREFIX_PHONE + "PHONE "
            + PREFIX_TAG + "TAG \n"
            + "Example: " + COMMAND_WORD + " johndoe41 "
            + PREFIX_NAME + "John Joe "
            + PREFIX_PHONE + "98765432 "
            + PREFIX_TAG + "RnD";;


    //+ PREFIX_EMAIL + "johndoe@example.com";
    //+ "[" + PREFIX_EMAIL + "EMAIL] "+ "[" + PREFIX_ADDRESS + "ADDRESS] "
    //+ "[" + PREFIX_TAG + "TAG]...\n"
    //+ "[" + PREFIX_ID + "ID] "

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited Person: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This person already exists in the address book.";

    private final Id id;
    private final EditPersonDescriptor editPersonDescriptor;
    private Person personToEdit;
    private Person editedPerson;

    /**
     * @param id of the person in the filtered person list to edit
     * @param editPersonDescriptor details to edit the person with
     */
    public EditCommand(Id id, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(id);
        requireNonNull(editPersonDescriptor);

        this.id = id;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        boolean isPersonExist = false;
        personToEdit = new Person(new Name("test"),
                new Id("test"), new Phone("123"), new HashSet<Tag>());

        for (int i = 0; i < lastShownList.size(); i++) {
            Person currentPerson = lastShownList.get(i);
            if (currentPerson.getId().equals(id)) {
                isPersonExist = true;
                personToEdit = currentPerson;
            }
        }

        if (!isPersonExist) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_ID);
        }
        assert !personToEdit.equals(new Person(new Name("test"), new Id("test"), new Phone("123"), new HashSet<>()))
                : "Should not reach here";
        editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        if (!personToEdit.isSamePerson(editedPerson) && model.hasPerson(editedPerson)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        for (Tag tag : editedPerson.getTags()) {
            if (!model.hasTag(tag)) {
                throw new CommandException(String.format(MESSAGE_TAG_NOT_IN_TAG_LIST, tag));
            }
        }

        model.setPerson(personToEdit, editedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        model.addExecutedCommand(this);
        return new CommandResult(String.format(MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)));
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor) {
        assert personToEdit != null;

        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        Phone updatedPhone = editPersonDescriptor.getPhone().orElse(personToEdit.getPhone());
        //Email updatedEmail = editPersonDescriptor.getEmail().orElse(personToEdit.getEmail());
        //Address updatedAddress = editPersonDescriptor.getAddress().orElse(personToEdit.getAddress());
        Set<Tag> updatedTags = editPersonDescriptor.getTags().orElse(personToEdit.getTags());
        Id updatedId = editPersonDescriptor.getId().orElse(personToEdit.getId());

        return new Person(updatedName, updatedId, updatedPhone, updatedTags);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand)) {
            return false;
        }

        EditCommand otherEditCommand = (EditCommand) other;
        return id.equals(otherEditCommand.id)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    /**
     * Retrieves the original {@code Person} instance that is targeted for editing.
     *
     * @return The {@code Person} instance that was initially marked for editing.
     */
    public Person getPersonToEdit() {
        return personToEdit;
    }

    /**
     * Retrieves the modified {@code Person} instance after edits have been applied.
     *
     * @return The {@code Person} instance that represents the edited state.
     */
    public Person getEditedPerson() {
        return editedPerson;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("id", id)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditPersonDescriptor {
        private Name name;
        private Phone phone;
        //private Email email;
        //private Address address;
        private Set<Tag> tags;
        private Id id;

        public EditPersonDescriptor() {}

        /**
         * Copy constructor.
         * A defensive copy of {@code tags} is used internally.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setName(toCopy.name);
            setPhone(toCopy.phone);
            //setEmail(toCopy.email);
            //setAddress(toCopy.address);
            setTags(toCopy.tags);
            setId(toCopy.id);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, phone, tags);
            //email, address, tags);
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        // public void setEmail(Email email) {
        //     this.email = email;
        // }

        // public Optional<Email> getEmail() {
        //     return Optional.ofNullable(email);
        // }

        // public void setAddress(Address address) {
        //     this.address = address;
        // }

        // public Optional<Address> getAddress() {
        //     return Optional.ofNullable(address);
        // }

        /**
         * Sets {@code tags} to this object's {@code tags}.
         * A defensive copy of {@code tags} is used internally.
         */
        public void setTags(Set<Tag> tags) {
            this.tags = (tags != null) ? new HashSet<>(tags) : null;
        }

        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code tags} is null.
         */
        public Optional<Set<Tag>> getTags() {
            return (tags != null) ? Optional.of(Collections.unmodifiableSet(tags)) : Optional.empty();
        }

        public void setId(Id id) {
            this.id = id;
        }

        public Optional<Id> getId() {
            return Optional.ofNullable(id);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor)) {
                return false;
            }

            EditPersonDescriptor otherEditPersonDescriptor = (EditPersonDescriptor) other;
            return Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(phone, otherEditPersonDescriptor.phone)
                    && Objects.equals(id, otherEditPersonDescriptor.id);
                    //&& Objects.equals(email, otherEditPersonDescriptor.email)
                    //&& Objects.equals(address, otherEditPersonDescriptor.address)
                    //&& Objects.equals(tags, otherEditPersonDescriptor.tags);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("id", id)
                    .add("phone", phone)
                    .add("tags", tags)
                    //.add("email", email)
                    //.add("address", address)
                    //.add("tags", tags)
                    .toString();
        }


    }
}
