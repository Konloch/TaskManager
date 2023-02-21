package com.konloch.taskmanager;

/**
 * A simple interface to return if a condition has been met
 *
 * @author Konloch
 * @since 01/15/2021
 */
public interface ConditionMet
{
	/**
	 * Returns true or false depending on the implementation
	 * @return if the specified condition has been met, it should return true
	 */
	boolean isConditionMet();
}
