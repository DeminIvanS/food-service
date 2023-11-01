package ru.liga.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import ru.liga.model.entities.Restaurant;

@Mapper
public interface RestaurantMapper {
    @Select(value = "select * from restaurants where id = #{id)")
    Restaurant findRestaurantById(@Param("id") Long id);
    @Select(value = "select * from restaurant where address = #{address}")
    Restaurant findRestaurantByAddress(@Param("address") String address);
}
