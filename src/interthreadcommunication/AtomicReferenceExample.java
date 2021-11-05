package interthreadcommunication;

import java.util.concurrent.atomic.AtomicReference;

/**
 * CompareAndSet operation (CAS):-
 * boolean compareAndSet(V expectedValue, V newValue)
 * Assigns newValue if current value == expectedValue
 * Ignores the newValue if the current value != expectedValue
 * <p>
 * CAS is available in all Atomic classes
 * Compiles into an atomic hardware operation
 * Many other atomic methods are internally implemented using CAS
 */

public class AtomicReferenceExample {
    public static void main(String[] args) {
        String oldName = "old name";
        String newName = "new name";
        AtomicReference<String> atomicReference = new AtomicReference<>(oldName);

        atomicReference.set("unexpected name");
        if (atomicReference.compareAndSet(oldName, newName)) { // atomically replace the value inside the AtomicReference with the newName
            System.out.println("New value is: " + atomicReference.get());
        } else {
            System.out.println("Nothing changed");
        }
    }
}
