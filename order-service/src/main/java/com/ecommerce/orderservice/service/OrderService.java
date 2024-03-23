package com.ecommerce.orderservice.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.ecommerce.orderservice.config.WebClientConfig;
import com.ecommerce.orderservice.dto.InventoryResponse;
import org.springframework.stereotype.Service;

import com.ecommerce.orderservice.dto.OrderLinesItemsDto;
import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderLineItems;
import com.ecommerce.orderservice.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class OrderService {
	private final OrderRepository orderRepository;

	private final WebClient.Builder webBuilderClient;

	public void placeOrder(OrderRequest orderRequest) {
		Order order = new Order();
		order.setOrderNumber(UUID.randomUUID().toString());
		List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
				.stream()
				.map(this::mapToDto)
				.toList();

		order.setOrderLineItemsList(orderLineItems);

		List<String> skuCodeList = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).toList();

		//making a http call for checking stock is available or not?
		InventoryResponse[] inventoryResponseArray = webBuilderClient.build().get().uri("http://inventory-service/api/inventory"
						,uriBuilder -> uriBuilder.queryParam("skuCode",skuCodeList).build())
						.retrieve().bodyToMono(InventoryResponse[].class).block();

		boolean allProductsIsInStock = false;
    	if(inventoryResponseArray != null && inventoryResponseArray.length>0){
		   allProductsIsInStock = Arrays.stream(inventoryResponseArray).
				   allMatch(InventoryResponse::isInStock);
	   }

		if(allProductsIsInStock)
			orderRepository.save(order);
		else
			throw new IllegalArgumentException("Product is not in stock, Please try again later!");
	}

	private OrderLineItems mapToDto(OrderLinesItemsDto orderLinesItemsDto) {
		OrderLineItems orderLineItems = new OrderLineItems();
		orderLineItems.setSkuCode(orderLinesItemsDto.getSkuCode());
		orderLineItems.setPrice(orderLinesItemsDto.getPrice());
		orderLineItems.setQuantity(orderLinesItemsDto.getQuantity());
		return orderLineItems;
	}

}
