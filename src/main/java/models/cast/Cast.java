package models.cast;
import bot.oldBot.Bot;

public class Cast {
    private Actor person;
    private Character character;

    public Actor getPerson() { return person; }
    public Character getCharacter() { return character; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Actor
        if (person != null && person.getName() != null && !person.getName().isEmpty()) {
            String actorName = person.getName().trim();
            if (person.getId() != -1) {
                String actorLink = Bot.bot_url + "?start=actor_" + person.getId();
                sb.append("Actor: [").append(actorName).append("](").append(actorLink).append(")\n");
            } else {
                sb.append("Actor: ").append(actorName).append("\n");
            }
        } else {
            sb.append("Actor: N/A\n");
        }

        // Character
        if (character != null && character.getName() != null && !character.getName().isEmpty()) {
            String characterName = character.getName().trim();
            if (character.getId() != -1) {
                String characterLink = Bot.bot_url + "?start=character_" + character.getId();
                sb.append("\tCharacter: [").append(characterName).append("](").append(characterLink).append(")\n");
            } else {
                sb.append("\tCharacter: ").append(characterName).append("\n");
            }
        } else {
            sb.append("\tCharacter: N/A\n");
        }

        return sb.toString();
    }

}