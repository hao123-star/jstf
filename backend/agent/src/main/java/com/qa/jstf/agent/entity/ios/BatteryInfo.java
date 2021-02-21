package com.qa.jstf.agent.entity.ios;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BatteryInfo {

    Integer level;

    Integer status;
}
