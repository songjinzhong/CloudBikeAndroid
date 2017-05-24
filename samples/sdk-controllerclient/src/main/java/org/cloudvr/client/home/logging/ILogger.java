package org.cloudvr.client.home.logging;

/**
 * A class that implements this interface is capable of receiving and handling log events.
 *
 * @author Pierfrancesco Soffritti
 */
public interface ILogger {
    void onLog(LoggerBus.Log log);
}