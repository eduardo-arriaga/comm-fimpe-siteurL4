package com.idear.fimpe.enums;

public enum BusRoute {
    //El campo routeId hace referencia al campo Numero de la tabla cRutas
    T01(1),
    T02(2),
    T03(3),
    T02A(16),
    NI_P(19),
    NI_R(20);

    private Integer routeId;

    BusRoute(Integer routeId) {
        this.routeId = routeId;
    }

    public Integer getRouteId(){
        return routeId;
    }
}
