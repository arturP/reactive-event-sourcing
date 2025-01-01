package io.artur.bank.base.application;

import akka.actor.typed.Behavior;
import akka.actor.typed.SpawnProtocol;
import akka.actor.typed.javadsl.Behaviors;

public abstract class SpawningBehavior {

    private SpawningBehavior() {
    }

    public static Behavior<SpawnProtocol.Command> create() {
        return Behaviors.setup(context -> SpawnProtocol.create());
    }
}
