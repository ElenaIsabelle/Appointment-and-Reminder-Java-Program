// Dispatcher.java
// Elena Phillips
// 10/28/2023
// A generic interface for dispatching messages.

package edu.fscj.cop2805c.dispatch;

public interface Dispatcher<T> {
    public void dispatch(T t);
}
