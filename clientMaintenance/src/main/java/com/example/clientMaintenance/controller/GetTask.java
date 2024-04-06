package com.example.clientMaintenance.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ch.qos.logback.core.net.server.Client;
import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.auth.SimpleAuthentication;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.zeebe.client.ZeebeClient;

@RestController
public class GetTask {

	@Autowired
	ZeebeClient zeebeclient;

	@PostMapping("/startWorkFlow")
	public String startWorkflow(@RequestBody String inputData) {
		System.out.println("inputData : " + inputData);

		zeebeclient.newCreateInstanceCommand().bpmnProcessId("taskListExample").latestVersion().variables(inputData)
				.send();

		return "User Task Flow Started";

	}

/////////////////////////// get assignee details ///////////////////////////////////////////////	

	@GetMapping("/getTask")
	public List<Task> getTask() throws TaskListException {
		SimpleAuthentication sa = new SimpleAuthentication("demo", "demo");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8083")
				.shouldReturnVariables().authentication(sa).build();

		List<Task> tasks = client.getAssigneeTasks(null, TaskState.CREATED, null);

//		 Task task = client.getTask(jobkey);
		return tasks;
	}

////////////////////////// Getting Details of Individual user Task - passing JobKey //////////	

	@GetMapping("/getTask/{jobkey}")
	public Task getTask(@PathVariable String jobkey) throws TaskListException {
		SimpleAuthentication sa = new SimpleAuthentication("demo", "demo");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8083")
				.shouldReturnVariables().authentication(sa).build();

		// List<Task> tasks = client.getAssigneeTasks("demo", TaskState.CREATED, null);

		Task task = client.getTask(jobkey);
		return task;
	}

///////////////////////// Cliam User Task //////////////////////////////////////////////////

	@GetMapping("/cliamTask/{jobKey}/{assigne}")
	public Task claimTask(@PathVariable String jobKey, @PathVariable String assigne) throws TaskListException {

		System.out.println("cliam Task");

		SimpleAuthentication sa = new SimpleAuthentication("demo", "demo");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8083")
				.shouldReturnVariables().authentication(sa).build();
		Task task = client.claim(jobKey, assigne);

		return task;

	}

/////////////////////////////// Complete user Task /////////////////////////////////////

	@GetMapping("/completeTask/{jokKey}")
	public Task completeTask(@PathVariable String jokKey) throws TaskListException {
		System.out.println("cliam Task");
//Map<String,Object> =
		SimpleAuthentication sa = new SimpleAuthentication("demo", "demo");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder().taskListUrl("http://localhost:8083")
				.shouldReturnVariables().authentication(sa).build();
		Task task = client.completeTask(jokKey, null);

		return task;

	}

}