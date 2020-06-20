// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public final class FindMeetingQuery { 
  
  // Queries ranges of time which meetings can be held based off already scheduled events and a meeting request //
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    // if requested duration is more than a day then there are no options for meeting times
    long duration = request.getDuration();

    if(duration == TimeRange.WHOLE_DAY.duration() + 1){
        Collection<TimeRange> query = new ArrayList<>();
        return query;
    }

    Collection<String> attendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();

    // if there are no mandatory attendees then optional attendees can be treated as mandatory 
    ArrayList<TimeRange> mandatorySchedule = attendees.size() == 0 ? getOptionalSchedule(events, optionalAttendees) : 
        getMandatorySchedule(events, attendees);
    ArrayList<TimeRange> optionalSchedule = attendees.size() == 0 || optionalAttendees.size() == 0 ? null : 
        getOptionalSchedule(events, optionalAttendees);
    
    // if there is potential to invite all optional attendees too, check and return that contrained
    // query instead
    ArrayList<TimeRange> query = getNotBusy(getBusy(mandatorySchedule), duration);
    return query.size() > 1 && optionalSchedule != null ? getOptionalQuery(query, optionalSchedule) : query;
  }

  // Returns time ranges where mandatory attendees already have a scheduled event
  private ArrayList<TimeRange> getMandatorySchedule(Collection<Event> events, Collection<String> attendees){
      ArrayList<Event> mandatorySchedule= new ArrayList<>();

      for(Event event: events){
          if(hasMandatoryAttendees(event, attendees)){
              mandatorySchedule.add(event);
          }
      }
      return getTimeRanges(mandatorySchedule);
  }

  // Returns time ranges where optional attendees already have a scheduled event
  private ArrayList<TimeRange> getOptionalSchedule(Collection<Event> events, Collection<String> optionalAttendees){
      ArrayList<Event> optionalSchedule= new ArrayList<>();

      for(Event event: events){
          if(hasOptionalAttendees(event, optionalAttendees)){
              optionalSchedule.add(event);
          }
      }
      return getTimeRanges(optionalSchedule);
  }

  // Checks if scheduled event has any requested required attendees //
  private boolean hasMandatoryAttendees(Event event, Collection<String> attendees){
      return Collections.disjoint(event.getAttendees(), attendees) ? false : true;    
  }
  
  // Checks if scheduled event has any requested optional attendees //  
  private boolean hasOptionalAttendees(Event event, Collection<String> optionalAttendees){
      return Collections.disjoint(event.getAttendees(), optionalAttendees) ? false : true;
  }

  // Returns sorted time ranges when events are being held //
  private ArrayList<TimeRange> getTimeRanges(ArrayList<Event> schedule){
      ArrayList<TimeRange> timeRanges = new ArrayList<>();

      for(Event event: schedule){
          timeRanges.add(event.getWhen());
      }
      Collections.sort(timeRanges, TimeRange.ORDER_BY_START);
      return timeRanges; 
  }

  // Returns time ranges when no meetings can be held //
  private ArrayList<TimeRange> getBusy(ArrayList<TimeRange> schedule){
      ArrayList<TimeRange> busy = new ArrayList<>();
      
      if(schedule.size() == 0){
          return busy;
      }

      if(schedule.size() == 1) {
          busy.add(TimeRange.fromStartDuration(schedule.get(0).start(), schedule.get(0).duration()));
          return busy;
      }

      int i = 0;
      int j = 1;
      
      TimeRange i_when = schedule.get(i);
      TimeRange j_when = schedule.get(j);

      int start = i_when.start();
      int end = i_when.end(); 
      int duration = i_when.duration();

      while(i < schedule.size() && j < schedule.size()){
          i_when = schedule.get(i);
          j_when = schedule.get(j);
                
          int j_end = j_when.end();

          boolean overlap = i_when.overlaps(j_when);

          if(overlap && j_end > end){
              end = j_end;
              duration = end - start;
              schedule.remove(j_when);
              j--;
          }else if(overlap) {
              schedule.remove(j_when);
              j--;
          }else{
              busy.add(TimeRange.fromStartDuration(start, duration));
              start = j_when.start();
              end = j_end;
              duration = j_when.duration();
              i++;
          }
          j++;
      }

      int size = schedule.size();

      if(size == 1){
       busy.add(TimeRange.fromStartDuration(start, duration));
      }

      int lastIndexSchedule = size - 1;
      int lastIndexBusy = busy.size() - 1;

      if(!busy.get(lastIndexBusy).overlaps(schedule.get(lastIndexSchedule))){
          busy.add(TimeRange.fromStartDuration(schedule.get(lastIndexSchedule).start(), 
          schedule.get(lastIndexSchedule).duration()));
      }

      return busy;
  }
      
  
  // Inverses the array list of 'busy' time ranges and return time ranges that mandatory attendees //
  // can attend that meet duration request //
  private ArrayList<TimeRange> getNotBusy(ArrayList<TimeRange> busy, long duration){
      ArrayList<TimeRange> notBusy= new ArrayList<>();

      int start = 0;
      int end = 0;
      int length = 0;

      for(TimeRange when: busy){
          end = when.start();
          length = end - start;
          if(length >= duration){
              notBusy.add(TimeRange.fromStartDuration(start, length));
          }
          start = when.end();
      }

      end = TimeRange.END_OF_DAY + 1;
      length = end - start;

      if(length >= duration){
          notBusy.add(TimeRange.fromStartDuration(start, length));
      }
      return notBusy;
  }

  // If the query can be constrained to include time ranges that allow optional attendees to attend too // 
  // then that query will be returned instead //
  private ArrayList<TimeRange> getOptionalQuery(ArrayList<TimeRange> query, ArrayList<TimeRange> optionalSchedule){
      ArrayList<TimeRange> tmpQuery= new ArrayList<>();

      int optionalScheduleIndex;
      int optionalScheduleSize = optionalSchedule.size();
      boolean noConflict = true;
      TimeRange time;

      for(TimeRange when: query){
          optionalScheduleIndex = 0;
          while(noConflict && optionalScheduleIndex < optionalScheduleSize){
              time = optionalSchedule.get(optionalScheduleIndex);
              if(when.overlaps(time)){
                  noConflict = false;
              }
              optionalScheduleIndex++;
          }
          if(noConflict){
              tmpQuery.add(when);
          }
          noConflict = true;
      }
      return tmpQuery.size() > 0 ? tmpQuery : query;
  }  
}