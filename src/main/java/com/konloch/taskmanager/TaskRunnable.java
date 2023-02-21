package com.konloch.taskmanager;

/**
 * The only reason you need this over Runnable is,
 * so you can stop the task at any time, like "break" would be.
 *
 * @author Konloch
 * @since 5/2/2021
 */
public interface TaskRunnable
{
	/**
	 * Run the specified task
	 * @param task the task being ran
	 */
	void run(Task task);
}
