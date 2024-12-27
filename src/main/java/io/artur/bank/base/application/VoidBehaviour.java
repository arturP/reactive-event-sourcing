package io.artur.bank.base.application;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public class VoidBehaviour {

    public static Behavior<Void> create() {
        return Behaviors.receive(Void.class).build();
    }
}
