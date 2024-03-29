package at.htlleonding.frontend.model;

public record EntryContent(String name, String points) {

    @Override
    public String toString(){
        return String.format("%1$s: %2$10s", this.name, this.points);
    }
}
