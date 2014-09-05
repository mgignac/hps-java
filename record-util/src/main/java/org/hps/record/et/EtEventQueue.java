package org.hps.record.et;

import org.hps.record.AbstractRecordQueue;
import org.jlab.coda.et.EtEvent;

/**
 * A dynamic queue for supplying <tt>EtEvent</tt> objects to a loop.
 * This would most likely be run on a separate thread than the 
 * loop to avoid undesired blocking behavior.
 */
public final class EtEventQueue extends AbstractRecordQueue<EtEvent> {

	/**
	 * Get the class of the record that is supplied.
	 * @return The class of the supplied records.
	 */
    @Override
    public Class<EtEvent> getRecordClass() {
        return EtEvent.class;
    }
}