package com.sprockitconsulting.vrslcm.plugin.products;

import com.sprockitconsulting.vrslcm.plugin.scriptable.Request;

/**
 * Implementing this interface indicates the Product supports LCM Power Cycle operations.
 */
public interface IPowerCycleSupport {
	Request powerOn();
	Request powerOff();
}
