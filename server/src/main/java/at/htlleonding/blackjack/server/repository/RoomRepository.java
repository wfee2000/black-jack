package at.htlleonding.blackjack.server.repository;

import at.htlleonding.blackjack.server.game.Dealer;

import java.util.ArrayList;
import java.util.List;

public class RoomRepository {
    private final List<Dealer> rooms;
    private static RoomRepository instance;
    private final List<Integer> roomIds;

    private RoomRepository() {
        rooms = new ArrayList<>();
        roomIds = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            roomIds.add(i);
        }
    }

    public static RoomRepository getInstance() {
        if (instance == null) {
            instance = new RoomRepository();
        }

        return instance;
    }

    public Dealer addRoom(int rounds) {
        if (roomIds.isEmpty()) {
            return null;
        }

        Dealer newDealer = new Dealer(roomIds.remove(0), rounds);
        rooms.add(newDealer);
        return newDealer;
    }

    public Dealer getRoom(int id) {
        if (id >= 1000) {
            return null;
        }

        return rooms.stream().filter(dealer -> dealer.getId() == id).findFirst().orElse(null);
    }

    public List<Dealer> getRooms() {
        return new ArrayList<>(rooms);
    }

    public boolean removeRoom(int id) {
        if (id >= 1000) {
            return false;
        }

        Dealer dealerToRemove = getRoom(id);

        if (dealerToRemove == null || dealerToRemove.getPlayerCount() != 0) {
            return false;
        }

        return rooms.remove(dealerToRemove);
    }
}
