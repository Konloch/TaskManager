# TaskManager
TaskManager is an easy to use zero dependency task manager / task queue.

## How To Add As Library
Add it as a maven dependency or just [download the latest release](https://github.com/Konloch/TaskManager/releases).
```xml
<dependency>
  <groupId>com.konloch</groupId>
  <artifactId>TaskManager</artifactId>
  <version>1.0.0</version>
</dependency>
```

## How To Use
For a more in-depth example of how to use the TaskManager, [view the test file](https://github.com/Konloch/TaskManager/blob/main/src/test/java/com/konloch/TestTaskManager.java#L26).
```java
//create and start a new task manager
TaskManager manager = new TaskManager();
manager.start();

//run the code after a delay of 1 second
manager.delay(1000, (task)->{
	System.out.println("This will execute after a delay of 1 second");
});

//create a task that will run forever
manager.doForever((task)->
{
	//stop the task depending on some condition
	if(someCondition)
		task.stop();
	else
		System.out.println("This will execute until stopped");
});
```