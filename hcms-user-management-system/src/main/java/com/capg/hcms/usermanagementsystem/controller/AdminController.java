package com.capg.hcms.usermanagementsystem.controller;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capg.hcms.usermanagementsystem.exceptions.PassKeyMisMatchException;
import com.capg.hcms.usermanagementsystem.exceptions.UserNotFoundException;
import com.capg.hcms.usermanagementsystem.model.Appointment;
import com.capg.hcms.usermanagementsystem.model.DiagnosticCenter;
import com.capg.hcms.usermanagementsystem.model.TestManagement;
import com.capg.hcms.usermanagementsystem.model.User;
import com.capg.hcms.usermanagementsystem.model.UserCredentials;
import com.capg.hcms.usermanagementsystem.service.IUserService;

@RestController

@RequestMapping("/admin")
public class AdminController {
	@Autowired
	IUserService userService;
	@PostMapping("/public/authenticate")
	public UserCredentials authenticateAdmin(@RequestBody UserCredentials userCredentials)
	{
		return userService.getUserCredentials(userCredentials);
	}
	

	
	public List<User> allUsersFallBack()
	{
		int num=0;
		  BigInteger number= BigInteger.valueOf(num);
		  Integer value=Integer.valueOf(num);
		  User user=new User("NOT AVAILABLE","NOT AVAILABLE","",number,"xyz@gmail.com","",value,"","");
       List<User> userList=new ArrayList();
       userList.add(user);
       return userList;
	}
    
	@RequestMapping("/deleteallusers")
	public boolean deleteAllUsers()
	{
		 userService.deleteAllUsers();
		 return true;
	}
    
	@DeleteMapping("/deleteuser/userId/{userId}")
	public boolean deleteUser(@PathVariable String userId)
	{
		return userService.deleteUser(userId);
	}
    
    @GetMapping("admin/getuser/userId/{userId}")
	public User getUserById(@PathVariable String userId)
	{
		return userService.getUserById(userId);
	}
	
	@PostMapping("/addcenter")
	public DiagnosticCenter addCenter(@RequestBody DiagnosticCenter center) 
	{
	return userService.addCenter(center);
	}

	@GetMapping("/getallcenters")
	public List<DiagnosticCenter> getAllCenters()
	{
		return userService.getAllCenters();
	}
	
	@DeleteMapping("/deleteallcenters")
	public boolean deleteAllCenters()
	{
	return userService.deleteAllCenters();	
	}
	@GetMapping("/getcenter/centerid/{centerId}")
	public DiagnosticCenter getCenterById(@PathVariable String centerId)
	{
	return userService.getCenterById(centerId);	
	}
	
	@DeleteMapping("/deletecenter/centerid/{centerId}")
	public boolean deleteCenterById(@PathVariable String centerId)
	{
	return userService.deleteCenterById(centerId);	
	}
	
	@GetMapping("/getalltests")
		public List<TestManagement> getAllTests(){
			return userService.getAllTests();
		}
	
	
	public List<TestManagement> allTestsByCenterId(String centerId)
	{
	TestManagement test=new TestManagement("NOT AVAILABLE","NOT AVAILABLE");
	List<TestManagement> testList=new ArrayList<>();
	testList.add(test);
	return testList;
	}
	@PostMapping("/addtest/centerid/{centerId}")
	public TestManagement addTest(@PathVariable String centerId,@RequestBody TestManagement newTest) 
		{
		return userService.addTest(centerId,newTest);
		}
	@DeleteMapping("/deletetest/centerid/{centerId}/testid/{testId}")
	public boolean deleteTestById(@PathVariable String centerId,@PathVariable  String testId) 
	{
		userService.deleteTestById(centerId,testId);
		return true;
	}
	@GetMapping("/gettest/testid/{testId}")
	public TestManagement getTestById(@PathVariable String testId)
	{
	return userService.getTestById(testId);
	}
	@DeleteMapping("/deletealltests")
	public boolean deleteAllTests()
	{
	return userService.deleteAllTests();	
	}
	@GetMapping("/getallappointments")
	public List<Appointment> getAllAppointments()
	{
		return userService.getAllAppointments();
	}
	
	@PutMapping("/approveappointment/{appointmentId}/{status}")
	public Appointment approveAppointment(@PathVariable BigInteger appointmentId,@PathVariable boolean status)
	{
		return userService.approveAppointment(appointmentId, status);
	}
	
	@GetMapping("/getappointmentsbycenterid/{centerId}")
	public List<Appointment> getAllAppointmentsByCenterId(@PathVariable String centerId)
	{
		return userService.getAllAppointmentsByCenterId(centerId);
	}
	
	
	@PostMapping("/registeradmin")
	public User registerAdmin(@RequestBody User user) throws PassKeyMisMatchException
	{
	return userService.registerAdmin(user);	
	}
	@GetMapping("/login/{userId}/{userPassword}")
	public User login(@PathVariable String userId,@PathVariable String userPassword) throws UserNotFoundException
	{
	return userService.login(userId,userPassword);	
	}
}