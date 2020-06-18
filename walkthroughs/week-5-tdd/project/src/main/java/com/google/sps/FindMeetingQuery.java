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
    ArrayList<Event> mandatorySchedule = attendees.size() == 0 ? getOptionalSchedule(events, optionalAttendees) : 
        getMandatorySchedule(events, attendees);
    ArrayList<Event> optionalSchedule = attendees.size() == 0 || optionalAttendees.size() == 0 
        ? null : getOptionalSchedule(events, optionalAttendees);

    ArrayList<TimeRange> busy= getBusy(mandatorySchedule);
    ArrayList<TimeRange> query= getNotBusy(busy, duration);

    // if there is potential to invite all optional attendees too, check and return that contrained
    // query
    return query.size() > 1 && optionalSchedule != null ? getOptionalQuery(query, optionalSchedule) : query;
  }

  // Seperates already scheduled events that have requested mandatory attendees from the rest of the scheduled events //
  private ArrayList<Event> getMandatorySchedule(Collection<Event> events, Collection<String> attendees){
      ArrayList<Event> mandatorySchedule= new ArrayList<>();

      for(Event event: events){
          if(hasMandatoryAttendees(event, attendees)){
              mandatorySchedule.add(event);
          }
      }
      return mandatorySchedule;
  }

  // Seperates already scheduled events that have optional attendees from the rest of the scheduled events //
  private ArrayList<Event> getOptionalSchedule(Collection<Event> events, Collection<String> optionalAttendees){
      ArrayList<Event> optionalSchedule= new ArrayList<>();

      for(Event event: events){
          if(hasOptionalAttendees(event, optionalAttendees)){
              optionalSchedule.add(event);
          }
      }
      return optionalSchedule;
  }

  // Checks if scheduled event has any requested required attendees //
  private boolean hasMandatoryAttendees(Event event, Collection<String> attendees){
      return Collections.disjoint(event.getAttendees(), attendees) ? false : true;    
  }
  
  // Checks if scheduled event has any requested optional attendees //  
  private boolean hasOptionalAttendees(Event event, Collection<String> optionalAttendees){
      return Collections.disjoint(event.getAttendees(), optionalAttendees) ? false : true;
  }

  // Returns an array list of time ranges which no meeting can be held //
  private ArrayList<TimeRange> getBusy(ArrayList<Event> mandatorySchedule){
      ArrayList<TimeRange> busy= new ArrayList<>();

      for(int i = 0; i < mandatorySchedule.size(); i++){
        Event i_event = mandatorySchedule.get(i);
        TimeRange i_when = i_event.getWhen();
        int i_start = i_when.start();
        int i_end = i_when.end();
        for(int j = i + 1; j < mandatorySchedule.size(); j++){
            Event j_event = mandatorySchedule.get(j);
            TimeRange j_when = j_event.getWhen();
            int j_start = j_when.start();
            int j_end = j_when.end();
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
        int i_length = i_end - i_start;
        busy.add(TimeRange.fromStartDuration(i_start, i_length));
    }
    return busy;
  }
  
  // Inverses the array of 'busy' time ranges and return time ranges that mandatory attendees can attend //
  private ArrayList<TimeRange> getNotBusy(ArrayList<TimeRange> busy, long duration){
      ArrayList<TimeRange> notBusy= new ArrayList<>();

      int start = 0;
      int end = 0;
      int length = 0;

      for(TimeRange when: busy){
          end = when.start();
          length = end - start;
          if(end - start >= duration){
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

  // If the query can be constrained to include time ranges that allow optional attendees to attend too then that 
  // query will be returned instead
  private ArrayList<TimeRange> getOptionalQuery(ArrayList<TimeRange> query, ArrayList<Event> optionalSchedule){
      ArrayList<TimeRange> tmpQuery= new ArrayList<>();

      boolean noConflicts= true;

      for(TimeRange when: query){
          for(Event event: optionalSchedule){
              if(when.overlaps(event.getWhen())){
                  noConflicts = false;
              }
          }
          if(noConflicts){
              tmpQuery.add(when);
          }
          noConflicts = true;
      }
      
      if(tmpQuery.size() > 0){
          return tmpQuery;
      }
      return query;
  }
}