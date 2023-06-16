package at.htlleonding.frontend.model;

public record RoomContent(String currentPlayers, String maxPlayers, String id, String name, String password) {

    @Override
    public String toString(){
        return String.format("[%s] %s/%s", name, currentPlayers, maxPlayers);
    }
}