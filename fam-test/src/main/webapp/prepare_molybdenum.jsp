<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="de.knurt.fam.test.web.PrepareDatabase"%>
<div>
	NEVER EVER USE THIS <strong>NOT</strong> AT HOME!!!<br />
	THIS KILLS DATABASE ENTRIES FOR TESTING REASONS ...<br />
	ONLY FOR USE WITH MOLYBDENUM TESTING ON TEST DATABASES.
</div>
<h1>Output</h1>
<div>
<%= PrepareDatabase.getInstance().doAndGetFeedback(request)%>
</div>
<h1>Reset</h1>
<form action=""><input type="hidden" name="confirm" value="1"></input><input id="reset" type="submit" value="RESET - i know, what i do!!!" /></form>
<h1>Ticket 262</h1>
<form action=""><input type="hidden" name="confirm" value="262"></input><input  id="ticket_262" type="submit" value="Ticket 262 - i know, what i do!!!" /></form>
<h1>Ticket 340</h1>
<form action=""><input type="hidden" name="confirm" value="340"></input><input  id="ticket_340" type="submit" value="Ticket 340 - i know, what i do!!!" /></form>
<h1>setABookingSessionIsNow</h1>
<form action=""><input type="hidden" name="confirm" value="setABookingSessionIsNow"></input><input  id="setABookingSessionIsNow" type="submit" value="setABookingSessionIsNow - i know, what i do!!!" /></form>
