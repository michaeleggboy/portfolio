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

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public final class FindMeetingQuery { 
  
  // Queries ranges of time which meetings can be held based off already scheduled events and a meeting request //
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    // if requested duration is more than a day then there are no options for meeting times
    if(request.getDuration() == TimeRange.WHOLE_DAY.duration() + 1){
        Collection<TimeRange> query= new ArrayList<>();
        return query;
    }

    ArrayList<Event> schedule= new ArrayList<>(events);
    
    HashSet<String> mandatoryAttendees= new HashSet<>(request.getAttendees());
    HashSet<String> optionalAttendees= new HashSet<>(request.getOptionalAttendees());

    // if there are no mandatory attendees then optional attendees can be treated as mandatory 
    ArrayList<Event> mandatorySchedule;
    if(mandatoryAttendees.size() == 0){
        mandatorySchedule= schedule;
    }else{
        mandatorySchedule= seperateMandatorySchedule(schedule, mandatoryAttendees);
    }
    ArrayList<Event> optionalSchedule= schedule;

    ArrayList<TimeRange> busy= busy(mandatorySchedule);
    ArrayList<TimeRange> notBusy= notBusy(busy);
    ArrayList<TimeRange> query= checkDuration(notBusy, request.getDuration());
    if(query.size() > 1 && optionalAttendees.size() > 0 && !mandatorySchedule.equals(optionalSchedule)){
        query= canOptionalAttend(query, optionalSchedule);
    }

    return query;
  }

  // Seperates already scheduled events that have requested mandatory attendees from the rest of the scheduled events //
  public ArrayList<Event> seperateMandatorySchedule(ArrayList<Event> schedule, HashSet<String> mandatoryAttendees){
      ArrayList<Event> mandatorySchedule= new ArrayList<>();

      for(int i= 0; i < schedule.size(); i++){
          Event event= schedule.get(i);
          if(containsMandatoryAttendees(event, mandatoryAttendees)){
              mandatorySchedule.add(event);
              schedule.remove(event);
              i--;
          }
      }
      return mandatorySchedule;
  }

  // Checks if scheduled event has any requested required attendees //
  public boolean containsMandatoryAttendees(Event event, HashSet<String> mandatoryAttendees){    
      for(String attendee: event.getAttendees()){
          if(mandatoryAttendees.contains(attendee)){
              return true;
          }
      }
      return false;
  }

  // Returns an array list of time ranges which no meeting can be held //
  public ArrayList<TimeRange> busy(ArrayList<Event> mandatorySchedule){
      ArrayList<TimeRange> busy= new ArrayList<>();

      for(int i= 0; i < mandatorySchedule.size(); i++){
        Event i_event= mandatorySchedule.get(i);
        TimeRange i_when= i_event.getWhen();
        int i_start= i_when.start();
        int i_end= i_when.end();
        for(int j= i + 1; j < mandatorySchedule.size(); j++){
            Event j_event= mandatorySchedule.get(j);
            TimeRange j_when= j_event.getWhen();
            int j_start= j_when.start();
            int j_end= j_when.end();
             if(i_when.overlaps(j_when)){
                if(j_start < i_start){
                    i_start= j_start;
                }
                if(j_end > i_end){
                    i_end= j_end;
                }
                mandatorySchedule.remove(j_event);
                j--;
            }
        }
        busy.add(TimeRange.fromStartDuration(i_start, i_end - i_start));
    }
    return busy;
  }
  
  // Inverses the array of 'busy' time ranges and return time ranges that mandatory attendees can attend //
  public ArrayList<TimeRange> notBusy(ArrayList<TimeRange> busy){
      ArrayList<TimeRange> notBusy= new ArrayList<>();
      int start= 0;
      int END_OF_DAY= 1440;

      for(TimeRange when: busy){
          int end= when.start();
          if(end - start > 0){
              notBusy.add(TimeRange.fromStartDuration(start, end - start));
          }
          start= when.end();
      }
      if(END_OF_DAY - start > 0){
          notBusy.add(TimeRange.fromStartDuration(start, END_OF_DAY - start));
      }
      return notBusy;
  }

  // Constrains the query to potential time ranges that meet requested durations //
  public ArrayList<TimeRange> checkDuration(ArrayList<TimeRange> notBusy, long duration){
      ArrayList<TimeRange> longEnough= new ArrayList<>();

      for(TimeRange when: notBusy){
          if(when.duration() >= duration){
              longEnough.add(when);
          }
      }
      return longEnough;
  } 

  // If the query can be constrained to include time ranges that allow optional attendees to attend too then that 
  // query will be returned instead
  public ArrayList<TimeRange> canOptionalAttend(ArrayList<TimeRange> query, ArrayList<Event> optionalSchedule){
      ArrayList<TimeRange> tmpQuery= new ArrayList<>();
      boolean noConflicts= true;

      for(TimeRange when: query){
          for(Event event: optionalSchedule){
              if(when.overlaps(event.getWhen())){
                  noConflicts= false;
              }
          }
          if(noConflicts){
              tmpQuery.add(when);
          }
          noConflicts= true;
      }
      
      if(tmpQuery.size() > 0){
          return tmpQuery;
      }
      return query;
  }
}
