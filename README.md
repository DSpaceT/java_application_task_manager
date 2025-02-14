INSTALLATION 
Prerequisites
Java JDK 17 or higher installed on your system.
Apache Maven installed.
An active internet connection (for the first build to download dependencies).
Installation
1) clone the repository:
git clone https://github.com/yourusername/myproject.git
cd myproject
2) cd to the myproject directory of the cloned repository.
3) Build the project :
mvn clean install
4) Run the application :
mvn javafx:run
Additional information for used libraries:
The project uses JavaFX dependencies (javafx-controls and javafx-fxml version 23.0.1) managed by Maven.
The main class for the application is configured as com.example.App in the pom.xml file.
The project also uses the org.json:json library (version 20210307).


TASK MANAGER DESCRIPTION
The task manager is an application that allows users to plan future obligations and activities for organizational purposes. Specifically, the user can add tasks for which they can set a title, description, deadline, priority level, task category, and status.
![task_manager_layout](https://github.com/user-attachments/assets/b8b58f38-b0e4-41fb-a36a-10bf0fa5b8ce)
The status of a task can be open, in_progress, delayed, completed, or postponed, with the delayed status being assigned automatically based on the task's deadline.

At the top of the home page, there is a bar displaying the total number of tasks, the number of completed tasks, the number of delayed tasks, and the number of tasks with a deadline within the next 7 days.
![top_bar](https://github.com/user-attachments/assets/af3c52eb-f635-4c2c-a7fe-83a245d641e6)
Additionally, we can add tasks by clicking the button in the bottom left corner, which opens an overlay where we can fill in the corresponding fields to create a task.
![add_task_overlay](https://github.com/user-attachments/assets/6b42506a-d7b7-4dae-99e0-0dd321567abc)
Additionally, for each task, we can delete it using the delete button, modify it using the modify button, and change its status either by selecting the desired status directly from the status bar or by using the toggle button, which switches the status from in_progress to completed.
![uncompleted_task](https://github.com/user-attachments/assets/ff3603b2-19b6-4df6-a244-543cf49014eb)
![completed_task](https://github.com/user-attachments/assets/9fd62b2c-1164-40b3-917e-9f7e33e4d3c2)
Additionally, at the top of the screen, there is a search bar where we can filter tasks based on category, priority, and title, or a combination of these criteria. We simply select the desired filters and click the apply button to perform the search.
![search_bar](https://github.com/user-attachments/assets/10fc3795-869a-4dcd-a764-94502db3c03c)
Additionally, we can add notifications for a task, but only if it is not delayed and the notification date is in the future and before the task's deadline. To do this, we click the notify button on the task, which opens an overlay for adding a notification.
![add_notification_overlay](https://github.com/user-attachments/assets/33e37f71-10e9-4d9e-9cef-aebbbef5dcb8)

The available predefined notification options are:

ONE_DAY_BEFORE
ONE_WEEK_BEFORE
ONE_MONTH_BEFORE
RIGHT_NOW (for testing, triggers a notification in 5 seconds)
Custom date (set by the user)
A notification cannot be set for a completed task. Once a notification expires, a corresponding message appears on the task, along with an exclamation mark next to the notifications button, which leads to a page containing all notifications to alert the user.
![notification_for_user](https://github.com/user-attachments/assets/b637debf-8ed9-4d12-9813-a3b2a95bc04d)
By clicking the notifications button, the user is redirected to the corresponding page, where they can use the available buttons to remove notifications (whether expired or not) and modify a notification. When modifying a notification, an overlay similar to the one used for creating the notification opens.
![notifications_page](https://github.com/user-attachments/assets/ef4ddf46-582d-4a86-80c1-5008ec76d041)
On the home page, the user can also add and remove categories for tasks, as well as priority levels. This can be done by clicking the "Manage Categories" button located at the top right corner of the home page.

Clicking this button opens an overlay where the user can:

Add categories and priorities with custom names.
Modify the name of an existing category or priority without losing the tasks associated with it.
Delete a category or priority, which will also delete all tasks associated with it.
![manage_categories](https://github.com/user-attachments/assets/da2fcbdc-2462-4022-95c2-1ecdfe0f8c59)

