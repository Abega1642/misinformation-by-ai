package dev.razafindratelo.misinformation.event.consumer;

import dev.razafindratelo.misinformation.InfraGenerated;
import dev.razafindratelo.misinformation.event.model.InfraEvent;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@InfraGenerated
@Component
public class EventDispatcher implements Consumer<InfraEvent> {
  @Override
  public void accept(InfraEvent infraEvent) {}
}
