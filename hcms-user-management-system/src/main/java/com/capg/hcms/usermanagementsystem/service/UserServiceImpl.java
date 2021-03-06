package com.capg.hcms.usermanagementsystem.service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.handler.UserRoleAuthorizationInterceptor;

import com.capg.hcms.usermanagementsystem.exceptions.ContactNumberAlreadyExistException;
import com.capg.hcms.usermanagementsystem.exceptions.EmailAlreadyExistException;
import com.capg.hcms.usermanagementsystem.exceptions.PassKeyMisMatchException;
import com.capg.hcms.usermanagementsystem.exceptions.UserEmailInvalidException;
import com.capg.hcms.usermanagementsystem.exceptions.UserNameAlreadyExistException;
import com.capg.hcms.usermanagementsystem.exceptions.UserNameInvalidException;
import com.capg.hcms.usermanagementsystem.exceptions.UserNotFoundException;
import com.capg.hcms.usermanagementsystem.exceptions.UserNumberInvalidException;
import com.capg.hcms.usermanagementsystem.exceptions.UserPasswordInvalidException;
import com.capg.hcms.usermanagementsystem.model.Appointment;
import com.capg.hcms.usermanagementsystem.model.DiagnosticCenter;
import com.capg.hcms.usermanagementsystem.model.TestManagement;
import com.capg.hcms.usermanagementsystem.model.User;
import com.capg.hcms.usermanagementsystem.model.UserCredentials;
import com.capg.hcms.usermanagementsystem.repo.UserRepo;

@Service
public class UserServiceImpl implements IUserService{
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private Random random;
	@Autowired
	private RestTemplate restTemplate;
	@Override
	public User registerUser(User user) throws UserNameInvalidException, 
	  UserPasswordInvalidException,UserEmailInvalidException, UserNumberInvalidException
	  ,UserNameAlreadyExistException,EmailAlreadyExistException 
	     {
		
		Pattern p1=Pattern.compile("[A-Z]{1}[a-zA-Z0-9]{6,14}$");
		Matcher m1=p1.matcher(user.getUserName());
		Pattern p2=Pattern.compile("^(?=.*[0-9])"+ "(?=.*[a-z])(?=.*[A-Z])"+ "(?=.*[@#$%^&+=])"+ "(?=\\S+$).{8,20}$");
		Matcher m2=p2.matcher(user.getUserPassword());
		Pattern p3=Pattern.compile("^(.+)@(.+)$");
		Matcher m3=p3.matcher(user.getUserEmail());
		Pattern p4=Pattern.compile("\\d{10}");
		Matcher m4=p4.matcher(user.getContactNumber().toString());
		if(!(m1.find() &&  m1.group().equals(user.getUserName())))
		{
			throw new UserNameInvalidException("Username should start with capital letter ad size should be 6-14  characters");
			
		}
		else if(!( m2.find() &&  m2.group().equals(user.getUserPassword())) )
		{
   			throw new UserPasswordInvalidException("User password must contain "
   					+ "capital letter,small letters and special character "
   					+ "without starting with number and range should be between 8 and 20");
		}
		else if(!( m3.find() &&  m3.group().equals(user.getUserEmail())) )
		{
   			throw new UserEmailInvalidException("user email is not valid");
		}
		else if(!( m4.find() &&  m4.group().equals(user.getContactNumber().toString())) )
		{
			throw new UserNumberInvalidException("contact number should contain 10 digits and starting may be 7,8 or 9");
		}
		else if(userRepo.getUserByUserName(user.getUserName())!=null)
			throw new UserNameAlreadyExistException("User with Name "+user.getUserName()+" already exist");
		
		else if(userRepo.getUserByContactNumber(user.getContactNumber())!=null)
			throw new ContactNumberAlreadyExistException("User with ContactNumber "+user.getContactNumber()+" already exist");
		
		else if(userRepo.getUserByUserEmail(user.getUserEmail())!=null)
			throw new EmailAlreadyExistException("User with Email "+user.getUserEmail()+" already exist");
		 else
			 user.setUserRole("customer");
			 user.setUserId(String.valueOf(random.nextInt(100000)).substring(0, 5));
		     userRepo.save(user);
		return user;
			
	}

	@Override
	public boolean deleteUser(String userId) {
		User user = userRepo.getOne(userId);
		if(user==null)
		{
			throw new UserNotFoundException("User Doesnot exist");
		}
		userRepo.deleteById(userId);
		return true;
	}

	@Override
	public User updateUser(User user) {
		User existingUser=userRepo.getOne(user.getUserId());
		if(existingUser==null)
		{
			throw new UserNotFoundException("User Doesnot exist");
		}
		existingUser.setUserName(user.getUserName());
		existingUser.setUserPassword(user.getUserPassword());
		existingUser.setUserEmail(user.getUserEmail());
		existingUser.setContactNumber(user.getContactNumber());
		return userRepo.save(existingUser);
	}

	@Override
	public User getUserById(String userId) {
		if(!userRepo.existsById(userId))
			throw new UserNotFoundException("User with id "+userId+" Not Found");
		return userRepo.getOne(userId);
	
	}

	@Override
	public List<User> getAllUsers() {
		if(userRepo.findAll().isEmpty())
		{
			throw new UserNotFoundException("Users unaivailable");
		}
		else {
			List<User> userList=userRepo.findAll();
			List<User> userRoleList=new ArrayList<>();
			for (User user : userList) {
				if(user.getUserRole().contains("customer"))
				{
					userRoleList.add(user);
					
				}
			}
			return userRoleList;
		}
		
	}

	@Override
	public boolean deleteAllUsers() {
		if(userRepo.findAll().isEmpty())
		{
			throw new UserNotFoundException("Users unaivailable");
		}
		 userRepo.deleteAll();
		 return true;
	}

	

	@Override
	public DiagnosticCenter addCenter(DiagnosticCenter center) {

		ResponseEntity<List<TestManagement>> testManage=restTemplate.exchange("http://localhost:8100/test/add-default", HttpMethod.GET,null,new ParameterizedTypeReference<List<TestManagement>>() {
		});
		
		List<TestManagement> listTest=testManage.getBody();
		 System.out.println(listTest);
		List<String> lists=new ArrayList();
		lists.add(listTest.get(0).getTestId());
		lists.add(listTest.get(1).getTestId());
		center.setTests(lists);
		DiagnosticCenter centerPosted=restTemplate.postForObject("http://localhost:8090/center/addcenter", center, DiagnosticCenter.class);	
		return centerPosted;
		

	}

	@Override
	public List<DiagnosticCenter> getAllCenters() {
		// TODO Auto-generated method stub
		ResponseEntity<List<DiagnosticCenter>> centerEntity=restTemplate.exchange("http://localhost:8090/center/getallcenters", HttpMethod.GET,null,new ParameterizedTypeReference<List<DiagnosticCenter>>() {
		});
		List<DiagnosticCenter> centerList=centerEntity.getBody();
		return centerList;
	}

	@Override
	public boolean deleteAllCenters() {
		// TODO Auto-generated method stub
		restTemplate.delete("http://localhost:8090/center/removeAll");
		return true;
	}

	@Override
	public DiagnosticCenter getCenterById(String centerId) {
		// TODO Auto-generated method stub
		DiagnosticCenter center=restTemplate.getForObject("http://localhost:8090/center/getcenter/center-Id/"+centerId,DiagnosticCenter.class);
		return center;
	}

	@Override
	public boolean deleteCenterById(String centerId) {
		// TODO Auto-generated method stub
		restTemplate.delete("http://localhost:8090/center/removecenter/centerId/"+centerId);
		return true;
	}

	@Override
	public List<TestManagement> getAllTests() {
		// TODO Auto-generated method stub
		ResponseEntity<List<TestManagement>> testEntity=restTemplate.exchange("http://localhost:8100/test/getAll", HttpMethod.GET,null,new ParameterizedTypeReference<List<TestManagement>>() {
		});
		List<TestManagement> testList=testEntity.getBody();
		return testList;
	}
	

	@Override
	public TestManagement addTest(String centerId,TestManagement newTest)  {
	
		DiagnosticCenter center=restTemplate.getForObject("http://localhost:8090/center/getcenter/center-Id/"+centerId,DiagnosticCenter.class);
		//System.out.println(center);
		
		if(center.getTests()==null)
		{
			List<String> testList=new ArrayList<>();
			testList.add(newTest.getTestId());	
		    center.setTests(testList);
		}
		else
		{
			center.getTests().add(newTest.getTestId());
		}
		
		TestManagement  addedTest=restTemplate.postForObject("http://localhost:8100/test/addTest",newTest,TestManagement.class);

		restTemplate.put(("http://localhost:8090/center/addtestid/"+centerId+"/testId/"+newTest.getTestId()), DiagnosticCenter.class);
		
		return  addedTest;
	}

	@Override
	public boolean deleteTestById(String centerId, String testId) {
		// TODO Auto-generated method stub
		DiagnosticCenter center= restTemplate.getForObject("http://localhost:8090/center/getcenter/center-Id/"+centerId,DiagnosticCenter.class);
		restTemplate.delete("http://localhost:8100/test/deleteTest/id/"+testId);
		restTemplate.put(("http://localhost:8090/center/remove-testid/"+centerId+"/test-id/"+testId), null);
		return true;
	}

	@Override
	public TestManagement getTestById(String testId) {
		// TODO Auto-generated method stub
		TestManagement existingTest=restTemplate.getForObject("http://localhost:8100/test/getTest/id/"+testId, TestManagement.class);
		return existingTest;
	}

	@Override
	public boolean deleteAllTests() {
		// TODO Auto-generated method stub
		
		restTemplate.delete("http://localhost:8100/test/deleteAll");
	//	restTemplate.delete("http://localhost:8090/removealltests");
		ResponseEntity<List<DiagnosticCenter>> centerEntity=restTemplate.exchange("http://localhost:8090/center/removealltests", HttpMethod.GET,null,new ParameterizedTypeReference<List<DiagnosticCenter>>() {});
		List<DiagnosticCenter> centerList=centerEntity.getBody();
		return true;
	}

	@Override
	public Appointment makeAppointment(String centerId,Appointment appointment) {
		
		Appointment newappointment = restTemplate.postForObject("http://localhost:8300/appointmentuser/makeappointment",appointment, Appointment.class);

		restTemplate.put(("http://localhost:8090/center/addappointmentid/"+centerId+"/appointmentid/"+ newappointment.getAppointmentId()), DiagnosticCenter.class);

		return newappointment;
		
	}

	@Override
	public List<Appointment> getAllAppointments() {
		// TODO Auto-generated method stub
		ResponseEntity<List<Appointment>> appointmentEntity=restTemplate.exchange("http://localhost:8300/appointmentuser/getallappointments", HttpMethod.GET,null,new ParameterizedTypeReference<List<Appointment>>() {
		});
		List<Appointment> appointmentList=appointmentEntity.getBody();
		return appointmentList;
	}

	@Override
	public Appointment approveAppointment(BigInteger appointmentId, boolean status) {
		// TODO Auto-generated method stub
		List<Appointment> appointmentList=getAllAppointments();
		restTemplate.put("http://localhost:8300/appointmentadmin/approveAppointment/" + appointmentId + "/status/" + status, null);
		Appointment approvee= restTemplate.getForObject("http://localhost:8300/appointmentadmin/getAppointment/" + appointmentId,
				Appointment.class);

		return approvee;
	
		
	}

	@Override
	public List<TestManagement> getAllTestsInACenter(String centerId) {
		System.out.println(centerId);
		DiagnosticCenter center = getCenterById(centerId);

		List<String> tests = center.getTests();
		List<TestManagement> testList = new ArrayList<TestManagement>();

		for (String testId : tests) {
			testList.add(getTestById(testId));
		}
		
		return testList;
		
	}

	@Override
	public List<Appointment> getAllAppointmentsByCenterId(String centerId) {
		// TODO Auto-generated method stub
		

			DiagnosticCenter center = getCenterById(centerId);
			BigInteger number=BigInteger.valueOf(1111111);
			List<BigInteger> appointments = center.getAppointments();
			List<Appointment> appointmentList = new ArrayList<Appointment>();
			
			for (BigInteger appointmentId : appointments) {
				appointmentList.add(getAppointment(appointmentId));
			}

			List<Appointment>newAppointmentList = new ArrayList(appointmentList);

			return newAppointmentList;
		
	}
	@Override
	public Appointment getAppointment(BigInteger appointmentId) {

		return restTemplate.getForObject("http://localhost:8300/appointmentadmin/getAppointment/" + appointmentId,
				Appointment.class);

	}

	@Override
	public User registerAdmin(User user) throws PassKeyMisMatchException {
		// TODO Auto-generated method stub
		user.setUserRole("admin");
		user.setUserId(String.valueOf(random.nextInt(1000)));
		System.out.println(user);
		if(user.getPassKey().equals("1223344"))
		{
			userRepo.save(user);
		}
		else
		{
			throw new PassKeyMisMatchException("INVALID PASSKEY");
		}
		return user;
	}

	@Override
	public UserCredentials getUserCredentials(UserCredentials credentials) {
		// TODO Auto-generated method stub
	String id=credentials.getUserId()+"";
	if(userRepo.existsById(id))
	{
		User user=userRepo.getOne(String.valueOf(credentials.getUserId()));
		return new UserCredentials(Integer.parseInt(user.getUserId()),user.getUserPassword(),user.getUserRole());
	 
	}
		
		
		
		
	/*	if(userRepo.getOne(String.valueOf(credentials.getUserId()))!=null)
		{
		User user=userRepo.getOne(String.valueOf(credentials.getUserId()));
		return new UserCredentials(Integer.parseInt(user.getUserId()),user.getUserPassword(),user.getUserRole());
		}*/
	return null;
	}

	@Override
	public User login(String userId, String password) throws UserNotFoundException {
		if(userRepo.existsById(userId))
		{
			User user=userRepo.getOne(userId);
			return user;
		}
		else
		{
			throw new UserNotFoundException("UserNotFound");
		}
	
	}

	
	
}
