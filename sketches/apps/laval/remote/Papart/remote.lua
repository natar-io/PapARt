local kb = libs.keyboard;


-- Documentation
-- http://www.unifiedremote.com/api

-- Keyboard Library
-- http://www.unifiedremote.com/api/libs/keyboard

--  FIRST_FLOOR_LIGHT = 1
--  FIRST_FLOOR_LIGHT_TOUCH = 2
--  SECOND_FLOOR_CINEMA = 4
--  SECOND_FLOOR_CAPTURE = 5


--@help Command 1
actions.first_floor_light = function ()
	kb.stroke("1");
end

actions.volume_up = function ()
   kb.stroke("keypadplus");
end

actions.volume_down = function ()
   kb.stroke("keypadminus");
end

--@help Command 2
actions.custom_first_floor = function ()
	kb.stroke("2");
end

actions.off = function ()
	kb.stroke("3");
end

--@help Command 3
actions.second_floor_cinema = function ()
	kb.stroke("4");
end

--@help Command 3
actions.second_floor_capture = function ()
	kb.stroke("5");
end
