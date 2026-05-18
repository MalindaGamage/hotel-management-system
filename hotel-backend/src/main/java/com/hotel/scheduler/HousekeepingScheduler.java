package com.hotel.scheduler;

import com.hotel.entity.HousekeepingTask;
import com.hotel.entity.HousekeepingTask.Priority;
import com.hotel.entity.HousekeepingTask.TaskStatus;
import com.hotel.entity.HousekeepingTask.TaskType;
import com.hotel.entity.Reservation;
import com.hotel.entity.Reservation.ReservationStatus;
import com.hotel.entity.Room;
import com.hotel.repository.HousekeepingTaskRepository;
import com.hotel.repository.HotelRepository;
import com.hotel.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HousekeepingScheduler {

    private final HotelRepository hotelRepository;
    private final ReservationRepository reservationRepository;
    private final HousekeepingTaskRepository taskRepository;

    /**
     * Every morning at 06:00, auto-create CLEANING tasks for rooms with checkouts today.
     * Skips rooms that already have a PENDING or IN_PROGRESS task for today.
     */
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void createDailyCleaningTasks() {
        LocalDate today = LocalDate.now();
        log.info("HousekeepingScheduler: creating cleaning tasks for {}", today);

        hotelRepository.findAll().forEach(hotel -> {
            List<Reservation> checkouts = reservationRepository.findExpectedDepartures(hotel.getId(), today);
            checkouts.forEach(reservation ->
                reservation.getReservationRooms().forEach(rr -> {
                    Room room = rr.getRoom();
                    boolean alreadyExists = taskRepository.existsByRoomIdAndScheduledDateAndStatusIn(
                        room.getId(), today, List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS));
                    if (!alreadyExists) {
                        HousekeepingTask task = HousekeepingTask.builder()
                            .room(room)
                            .taskType(TaskType.CLEANING)
                            .status(TaskStatus.PENDING)
                            .priority(Priority.HIGH)
                            .scheduledDate(today)
                            .notes("Auto-generated: checkout cleaning for reservation "
                                + reservation.getConfirmationNumber())
                            .build();
                        taskRepository.save(task);
                        log.debug("Created cleaning task for room {} hotel {}",
                            room.getRoomNumber(), hotel.getName());
                    }
                })
            );
        });
    }
}
