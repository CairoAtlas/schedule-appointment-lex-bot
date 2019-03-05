package com.github.cairoatlas;

import com.github.cairoatlas.objects.ValidationResult;
import com.github.cairoatlas.objects.request.LexRequest;
import com.github.cairoatlas.objects.response.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ScheduleAppointmentRequestHandler {

	private static final ZoneId EASTERN_TIME_ZONE = ZoneId.of("America/New_York");

	private static final List<String> DATE_FORMATS =
			Arrays.asList(
					"MMMM dd, yyyy",
					"MMMM dd, yy",
					"MMMM d, yyyy",
					"MMMM d, yy",
					"MMM dd, yyyy",
					"MMM dd, yy",
					"MMM d, yyyy",
					"MMM d, yy",
					"MM dd, yyyy",
					"MM dd, yy",
					"MM d, yyyy",
					"MM d, yy",
					"M dd, yyyy",
					"M dd, yy",
					"M d, yyyy",
					"M d, yy",
					"yyyy-MM-dd",
					"MM/dd/yyyy",
					"MM/dd/yy",
					"MM/d/yyyy",
					"MM/d/yy",
					"M/dd/yyyy",
					"M/dd/yy",
					"M/d/yyyy",
					"M/d/yy",
					"MM-dd-yyyy",
					"MM-dd-yy",
					"MM-d-yyyy",
					"MM-d-yy",
					"M-dd-yyyy",
					"M-dd-yy",
					"M-d-yyyy",
					"M-d-yy");

	private static final Map<String, Integer> APPOINTMENT_DURATION;

	static {
		Map<String, Integer> duration = new HashMap<>();
		duration.put("cleaning", 30);
		duration.put("root canal", 60);
		duration.put("whitening", 30);
		APPOINTMENT_DURATION = Collections.unmodifiableMap(duration);
	}

	private static final Map<String, List<String>> DAY_STRINGS;

	private static final Gson GSON = new Gson();

	static {
		Map<String, List<String>> days = new HashMap<>();
		days.put("MONDAY", Arrays.asList("Mon", "Monday", "0"));
		days.put("TUESDAY", Arrays.asList("Tue", "Tuesday", "1"));
		days.put("WEDNESDAY", Arrays.asList("Wed", "Wednesday", "2"));
		days.put("THURSDAY", Arrays.asList("Thu", "Thursday", "3"));
		days.put("FRIDAY", Arrays.asList("Fri", "Friday", "4"));
		days.put("SATURDAY", Arrays.asList("Sat", "Saturday", "5"));
		days.put("SUNDAY", Arrays.asList("Sun", "Sunday", "6"));
		DAY_STRINGS = Collections.unmodifiableMap(days);
	}

	private static final Map<String, String> MONTH_STRINGS;

	static {
		Map<String, String> months = new HashMap<>();
		months.put("JANUARY", "January");
		months.put("FEBRUARY", "February");
		months.put("MARCH", "March");
		months.put("APRIL", "April");
		months.put("MAY", "May");
		months.put("JUNE", "June");
		months.put("JULY", "July");
		months.put("AUGUST", "August");
		months.put("SEPTEMBER", "September");
		months.put("OCTOBER", "October");
		months.put("NOVEMBER", "November");
		months.put("DECEMBER", "Decmber");
		MONTH_STRINGS = Collections.unmodifiableMap(months);
	}

	private static final Logger LOG = LogManager.getLogger(ScheduleAppointmentRequestHandler.class);

	private LexResponse elicitSlot(
			final Map<String, String> outputSessionAttributes,
			final String intentName,
			final Map<String, String> slots,
			final String slotToElicit,
			final DialogActionMessage message,
			final ResponseCard responseCard) {
		DialogAction dialogAction = new DialogAction();
		dialogAction.setType("ElicitSlot");
		dialogAction.setIntentName(intentName);
		dialogAction.setMessage(message);
		dialogAction.setSlotToElicit(slotToElicit);
		dialogAction.setSlots(slots);
		dialogAction.setResponseCard(responseCard);

		LexResponse lexResponse = new LexResponse();
		lexResponse.setSessionAttributes(outputSessionAttributes);
		lexResponse.setDialogAction(dialogAction);

		return lexResponse;
	}

	private LexResponse confirmIntent(
			final Map<String, String> outputSessionAttributes,
			final String intentName,
			final Map<String, String> slots,
			final DialogActionMessage message,
			final ResponseCard responseCard) {
		DialogAction dialogAction = new DialogAction();
		dialogAction.setType("ConfirmIntent");
		dialogAction.setIntentName(intentName);
		dialogAction.setMessage(message);
		dialogAction.setSlots(slots);
		dialogAction.setResponseCard(responseCard);

		LexResponse lexResponse = new LexResponse();
		lexResponse.setSessionAttributes(outputSessionAttributes);
		lexResponse.setDialogAction(dialogAction);

		return lexResponse;
	}

	private LexResponse close(
			final Map<String, String> outputSessionAttributes, final DialogActionMessage message) {
		DialogAction dialogAction = new DialogAction();
		dialogAction.setType("Close");
		dialogAction.setFulfillmentState("Fulfilled");
		dialogAction.setMessage(message);

		LexResponse lexResponse = new LexResponse();
		lexResponse.setSessionAttributes(outputSessionAttributes);
		lexResponse.setDialogAction(dialogAction);

		return lexResponse;
	}

	private LexResponse delegate(
			final Map<String, String> outputSessionAttributes, final Map<String, String> slots) {
		DialogAction dialogAction = new DialogAction();
		dialogAction.setType("Close");
		dialogAction.setSlots(slots);

		LexResponse lexResponse = new LexResponse();
		lexResponse.setSessionAttributes(outputSessionAttributes);
		lexResponse.setDialogAction(dialogAction);

		return lexResponse;
	}

	private ResponseCard buildResponseCard(
			final String title, final String subtitle, final List<GenericAttachmentButton> options) {
		List<GenericAttachmentButton> buttons = new ArrayList<>();
		if (options != null && !options.isEmpty()) {
			buttons = new ArrayList<>();
			for (int i = 0; i < Math.min(5, options.size()); i++) {
				buttons.add(options.get(i));
			}
		}

		ResponseCard responseCard = new ResponseCard(1, "application/vnd.amazonaws.card.generic");
		GenericAttachment attachment = new GenericAttachment(title, subtitle);
		attachment.setButtons(buttons);
		responseCard.setGenericAttachments(Collections.singletonList(attachment));

		return responseCard;
	}

	private String incrementTimeByThirtyMinutes(final String appointmentTime) {
		String[] parts = appointmentTime.split(":");
		int hour = Integer.valueOf(parts[0]);
		int minutes = Integer.valueOf(parts[1]);

		if (minutes == 30) {
			hour++;
			return hour + ":00";
		}

		minutes += 30;
		return hour + ":" + minutes;
	}

	private int getRandomInt(final double minimum, final double maximum) {
		int minInt = (int) Math.ceil(minimum);
		int maxInt = (int) Math.floor(maximum);

		return ThreadLocalRandom.current().nextInt(minInt, maxInt);
	}

	private List<String> getAvailabilities(final String date) {
		int dayOfWeek =
				Integer.valueOf(
						DAY_STRINGS
								.get(parseDate(date).getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
								.get(2));
		List<String> availabilities = new ArrayList<>();
		double availabilityProbability = 0.3;
		if (dayOfWeek == 0) {
			int startHour = 10;
			while (startHour <= 16) {
				if (Math.random() < availabilityProbability) {
					int appointmentType = getRandomInt(1, 4);
					if (appointmentType == 1) {
						availabilities.add(startHour + ":00");
					} else if (appointmentType == 2) {
						availabilities.add(startHour + ":30");
					} else {
						availabilities.add(startHour + ":00");
						availabilities.add(startHour + ":30");
					}
				}

				startHour += 1;
			}
		}

		if (dayOfWeek == 2 || dayOfWeek == 4) {
			availabilities.add("10:00");
			availabilities.add("16:00");
			availabilities.add("16:30");
		}

		return availabilities;
	}

	private boolean isValidDate(final String dateString) {
		for (String dateFormat : DATE_FORMATS) {
			try {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
				simpleDateFormat.parse(dateString);
				return true;
			} catch (ParseException e) {
				// log e
			}
		}

		return false;
	}

	private LocalDate parseDate(final String dateString) {
		for (String dateFormat : DATE_FORMATS) {
			try {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
				Date input = simpleDateFormat.parse(dateString);
				return input.toInstant().atZone(EASTERN_TIME_ZONE).toLocalDate();
			} catch (ParseException e) {
				// log e
			}
		}

		throw new IllegalStateException("Invalid date");
	}

	private boolean isAvailable(
			final String appointmentTime, final int duration, final List<String> availabilities) {
		if (duration == 30) {
			return availabilities.contains(appointmentTime);
		} else if (duration == 60) {
			String secondHalfHourTime = incrementTimeByThirtyMinutes(appointmentTime);
			return availabilities.contains(appointmentTime)
					&& availabilities.contains(secondHalfHourTime);
		}

		throw new IllegalStateException("Was not able to understand duration " + duration);
	}

	private Integer getDuration(final String appointmentType) {
		return APPOINTMENT_DURATION.get(appointmentType.toLowerCase());
	}

	private List<String> getAvailabilitiesForDuration(
			final int duration, final List<String> availabilities) {
		List<String> durationAvailabilities = new ArrayList<>();
		String startTime = "10:00";
		while (!startTime.equals("17:00")) {
			if (availabilities.contains(startTime)) {
				if (duration == 30) {
					durationAvailabilities.add(startTime);
				} else if (availabilities.contains(incrementTimeByThirtyMinutes(startTime))) {
					durationAvailabilities.add(startTime);
				}
			}

			startTime = incrementTimeByThirtyMinutes(startTime);
		}

		return durationAvailabilities;
	}

	private ValidationResult validateBookAppointment(
			final String appointmentType, final String date, final String appointmentTime) {
		Integer duration = getDuration(appointmentType);
		if (appointmentType != null && !appointmentType.isEmpty() && duration != null) {
			return new ValidationResult(
					false,
					"AppointmentType",
					"I did not recognize that, can I book you a root canal, cleaning, or whitening?");
		}

		if (appointmentTime != null && !appointmentTime.isEmpty()) {
			if (appointmentTime.length() != 5) {
				return new ValidationResult(
						false,
						"Time",
						"I did not recognize that, what time would you like to book your appointment?");
			}

			String[] parts = appointmentTime.split(":");
			int hour = Integer.valueOf(parts[0]);
			int minute = Integer.valueOf(parts[1]);

			if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
				return new ValidationResult(
						false,
						"Time",
						"I did not recognize that, what time would you like to book your appointment?");
			}

			if (hour < 10 || hour > 16) {
				return new ValidationResult(
						false,
						"Time",
						"Our business hours are ten a.m. to five p.m.  What time works best for you?");
			}

			if (minute != 0 && minute != 30) {
				return new ValidationResult(
						false,
						"Time",
						"We schedule appointments every half hour, what time works best for you?");
			}
		}

		if (date != null && !date.isEmpty()) {
			if (!isValidDate(date)) {
				return new ValidationResult(
						false, "Date", "I did not understand that, what date works best for you?");
			} else if (parseDate(date).isBefore(LocalDate.now(EASTERN_TIME_ZONE))) {
				return new ValidationResult(
						false,
						"Date",
						"Appointments must be scheduled a day in advance.  Can you try a different date?");
			} else if ("SUNDAY"
					.equals(parseDate(date).getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
					|| "SATURDAY"
					.equals(
							parseDate(date).getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH))) {
				return new ValidationResult(
						false, "Date", "Our office is not open on the weekends, can you provide a work day?");
			}
		}

		return new ValidationResult(true, null, null);
	}

	private String buildTimeOutputString(final String appointmentTime) {
		String[] parts = appointmentTime.split(":");
		int hour = Integer.valueOf(parts[0]);
		if (hour > 12) {
			hour -= 12;
			return hour + ":" + parts[1] + " p.m.";
		} else if (hour == 12) {
			return "12:" + parts[1] + " p.m.";
		} else if (hour == 0) {
			return "12:" + parts[1] + " a.m.";
		}

		return hour + ":" + parts[1] + " a.m.";
	}

	private String buildAvailableTimeString(final List<String> availabilities) {
		String prefix = "We have availabilities at ";
		if (availabilities.size() > 3) {
			prefix = "We have plenty of availability, including ";
		}

		prefix += buildTimeOutputString(availabilities.get(0));
		if (availabilities.size() == 2) {
			return prefix + " and " + buildTimeOutputString(availabilities.get(1));
		}

		return prefix
				+ ", "
				+ buildTimeOutputString(availabilities.get(1))
				+ " and "
				+ buildTimeOutputString(availabilities.get(2));
	}

	private List<GenericAttachmentButton> buildOptions(
			final String slot,
			final String appointmentType,
			final String date,
			final Map<String, List<String>> bookingMap) {
		if (slot.equals("AppointmentType")) {
			return Arrays.asList(
					new GenericAttachmentButton("cleaning (30 min)", "cleaning"),
					new GenericAttachmentButton("root canal (60 min)", "root canal"),
					new GenericAttachmentButton("whitening (30 min)", "whitening"));
		} else if (slot.equals("Date")) {
			List<GenericAttachmentButton> options = new ArrayList<>();
			LocalDate potentialDate = LocalDate.now(EASTERN_TIME_ZONE);
			while (options.size() < 5) {
				potentialDate = potentialDate.plusDays(1);
				if (!"SUNDAY".equals(potentialDate.getDayOfWeek())
						&& !"SATURDAY".equals(potentialDate.getDayOfWeek())) {
					options.add(
							new GenericAttachmentButton(
									potentialDate.getMonthValue()
											+ "-"
											+ potentialDate.getDayOfMonth()
											+ DAY_STRINGS
											.get(
													potentialDate
															.getDayOfWeek()
															.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
											.get(0)
											+ " ("
											+ DAY_STRINGS
											.get(
													potentialDate
															.getDayOfWeek()
															.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
											.get(1)
											+ ")",
									DAY_STRINGS
											.get(
													potentialDate
															.getDayOfWeek()
															.getDisplayName(TextStyle.FULL, Locale.ENGLISH))
											.get(0)
											+ ", "
											+ MONTH_STRINGS.get(
											potentialDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
											+ " "
											+ potentialDate.getDayOfMonth()
											+ ", "
											+ potentialDate.getYear()));
				}
			}
			return options;
		} else if ("Time".equals(slot)) {
			if (appointmentType == null || appointmentType.isEmpty() || date == null || date.isEmpty()) {
				return null;
			}
			List<String> availabilities = bookingMap.get(date);
			if (availabilities == null) {
				return null;
			}

			availabilities = getAvailabilitiesForDuration(getDuration(appointmentType), availabilities);
			if (availabilities.isEmpty()) {
				return null;
			}
			List<GenericAttachmentButton> options = new ArrayList<>();
			for (int i = 0; i < Math.min(availabilities.size(), 5); i++) {
				options.add(
						new GenericAttachmentButton(
								buildTimeOutputString(availabilities.get(i)),
								buildTimeOutputString(availabilities.get(i))));
			}

			return options;
		}

		return null;
	}

	private LexResponse makeAppointment(LexRequest intentRequest) {
		String appointmentType = intentRequest.getCurrentIntent().getSlots().get("AppointmentType");
		String date = intentRequest.getCurrentIntent().getSlots().get("Date");
		String appointmentTime = intentRequest.getCurrentIntent().getSlots().get("Time");
		String source = intentRequest.getInvocationSource();
		Map<String, String> outputSessionAttributes = intentRequest.getSessionAttributes();
		// TODO: make sure this is built properly
		Type bookingMapType = new TypeToken<Map<String, List<String>>>() {
		}.getType();
		Map<String, List<String>> bookingMap =
				GSON.fromJson(outputSessionAttributes.get("bookingMap"), bookingMapType);

		if (source.equals("DialogCodeHook")) {
			Map<String, String> slots = intentRequest.getCurrentIntent().getSlots();
			ValidationResult validationResult =
					validateBookAppointment(appointmentType, date, appointmentTime);
			if (!validationResult.isValid()) {
				slots.put(validationResult.getViolatedSlot(), null);
				return elicitSlot(
						outputSessionAttributes,
						intentRequest.getCurrentIntent().getName(),
						slots,
						validationResult.getViolatedSlot(),
						validationResult.getDialogActionMessage(),
						buildResponseCard(
								"Specify " + validationResult.getViolatedSlot(),
								validationResult.getDialogActionMessage().getContent(),
								buildOptions(
										validationResult.getViolatedSlot(), appointmentType, date, bookingMap)));
			}

			if (appointmentType == null || appointmentType.isEmpty()) {
				validationResult
						.getDialogActionMessage()
						.setContent("What type of appointment woud you like to schedule?");
				return elicitSlot(
						outputSessionAttributes,
						intentRequest.getCurrentIntent().getName(),
						slots,
						"AppointmentType",
						validationResult.getDialogActionMessage(),
						buildResponseCard(
								"Specify Appointment Type",
								validationResult.getDialogActionMessage().getContent(),
								buildOptions("AppointmentType", appointmentType, date, new HashMap<>())));
			}

			if (date == null || date.isEmpty()) {
				validationResult
						.getDialogActionMessage()
						.setContent("When would you like to schedule your " + appointmentType + "?");

				return elicitSlot(
						outputSessionAttributes,
						intentRequest.getCurrentIntent().getName(),
						slots,
						"Date",
						validationResult.getDialogActionMessage(),
						buildResponseCard(
								"Specify Date",
								validationResult.getDialogActionMessage().getContent(),
								buildOptions("Date", appointmentType, date, new HashMap<>())));
			}

			List<String> bookingAvailabilties = bookingMap.get(date);
			if (bookingAvailabilties == null) {
				bookingAvailabilties = getAvailabilities(date);
				bookingMap.put(date, bookingAvailabilties);
				outputSessionAttributes.put("bookingMap", GSON.toJson(bookingMap));
			}

			List<String> appointmentTypeAvailabilities =
					getAvailabilitiesForDuration(getDuration(appointmentType), bookingAvailabilties);
			if (appointmentTypeAvailabilities.size() == 0) {
				slots.put("Date", null);
				slots.put("Time", null);
				validationResult
						.getDialogActionMessage()
						.setContent(
								"We do not have any availability on that date, is there another day which works for you?");

				return elicitSlot(
						outputSessionAttributes,
						intentRequest.getCurrentIntent().getName(),
						slots,
						"Date",
						validationResult.getDialogActionMessage(),
						buildResponseCard(
								"Specify Date",
								"What day works best for you?",
								buildOptions("Date", appointmentType, date, bookingMap)));
			}

			String content = "What time on " + date + "works for you?";
			validationResult.getDialogActionMessage().setContent(content);
			if (appointmentTime != null && !appointmentTime.isEmpty()) {
				outputSessionAttributes.put("formattedTime", buildTimeOutputString(appointmentTime));
				if (isAvailable(appointmentTime, getDuration(appointmentType), bookingAvailabilties)) {
					return delegate(outputSessionAttributes, slots);
				}

				content = "The time you requested is not available. ";
			}

			if (appointmentTypeAvailabilities.size() == 1) {
				validationResult
						.getDialogActionMessage()
						.setContent(
								content
										+ buildTimeOutputString(appointmentTypeAvailabilities.get(0))
										+ "is our only availability, does that work for you?");
				slots.put("Time", appointmentTypeAvailabilities.get(0));
				return confirmIntent(
						outputSessionAttributes,
						intentRequest.getCurrentIntent().getName(),
						slots,
						validationResult.getDialogActionMessage(),
						buildResponseCard(
								"Confirm Appointment",
								"Is "
										+ buildTimeOutputString(appointmentTypeAvailabilities.get(0))
										+ "on "
										+ date
										+ " okay?",
								Arrays.asList(
										new GenericAttachmentButton("yes", "yes"),
										new GenericAttachmentButton("no", "no"))));
			}

			String availableTimeString = buildAvailableTimeString(appointmentTypeAvailabilities);
			validationResult.getDialogActionMessage().setContent(content + availableTimeString);
			return elicitSlot(
					outputSessionAttributes,
					intentRequest.getCurrentIntent().getName(),
					slots,
					"Time",
					validationResult.getDialogActionMessage(),
					buildResponseCard(
							"Specify Time",
							"What time works best for you?",
							buildOptions("Time", appointmentType, date, bookingMap)));
		}

		// Book the appointment.  In a real bot, this would likely involve a call to a backend service.
		Integer duration = getDuration(appointmentType);
		List<String> bookingAvailabilities = bookingMap.get(date);
		if (bookingAvailabilities != null && !bookingAvailabilities.isEmpty()) {
			bookingAvailabilities.remove(appointmentTime);
			if (duration == 60) {
				String secondHalfHourTime = incrementTimeByThirtyMinutes(appointmentTime);
				bookingAvailabilities.remove(secondHalfHourTime);
			}
			bookingMap.put(date, bookingAvailabilities);
			outputSessionAttributes.put("bookingMap", GSON.toJson(bookingMap));
		} else {
			// This is not treated as an error as this code sample supports functionality either as
			// fulfillment or dialog code hook.
			LOG.debug(
					"Availabilities for {} were null at fulfillment time. " +
							"This should have been initialized if this function was configured as the dialog code hook",
					date);
		}
		DialogActionMessage dialogActionMessage = new DialogActionMessage();
		dialogActionMessage.setContent(
				"Okay, I have booked your appointment. We will see you at "
						+ buildTimeOutputString(appointmentTime)
						+ " on "
						+ date);
		return close(outputSessionAttributes, dialogActionMessage);
	}

	private LexResponse dispatch(final LexRequest intentRequest) {
		// TODO: logger.debug('dispatch userId={}, intentName={}'.format(intent_request['userId'],
		// intent_request['currentIntent']['name']))
		String intentName = intentRequest.getCurrentIntent().getName();
		LOG.debug("dispatch userId={}, intentName={}", intentRequest.getUserId(), intentName);

		if (intentName.equals("MakeAppointment")) {
			return makeAppointment(intentRequest);
		}

		throw new IllegalStateException("Intent with name " + intentName + " not supported");
	}

	public LexResponse handleRequest(
			final LexRequest requestEvent, Map<String, String> sessionAttributes) {
		LOG.debug("event.bot.name={}", requestEvent.getBot().getName());
		return dispatch(requestEvent);
	}
}
